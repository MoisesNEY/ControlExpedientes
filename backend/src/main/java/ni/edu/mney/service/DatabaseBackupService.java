package ni.edu.mney.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import jakarta.annotation.PostConstruct;
import ni.edu.mney.service.dto.DatabaseBackupFrequency;
import ni.edu.mney.service.dto.DatabaseBackupHistoryItemDTO;
import ni.edu.mney.service.dto.DatabaseBackupSettingsDTO;
import ni.edu.mney.service.dto.DatabaseBackupSummaryDTO;
import ni.edu.mney.service.dto.NotificacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DatabaseBackupService {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseBackupService.class);
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final String BACKUP_EXTENSION = ".backup";
    private static final String SQL_EXTENSION = ".sql";
    private static final String SETTINGS_FILENAME = "backup-settings.json";
    private static final int DEFAULT_INTERVAL_HOURS = 24;
    private static final int MAX_INTERVAL_HOURS = 720;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${application.database-backup.storage-path:${java.io.tmpdir}/control-expedientes/database-backups}")
    private String backupStoragePath;

    private final ObjectMapper objectMapper;
    private final Object backupLock = new Object();
    private final NotificacionService notificacionService;

    public DatabaseBackupService(ObjectMapper objectMapper, NotificacionService notificacionService) {
        this.objectMapper = objectMapper;
        this.notificacionService = notificacionService;
    }

    @PostConstruct
    void initialize() {
        synchronized (backupLock) {
            ensureStorageDirectory();
            StoredSettings settings = loadSettings();
            LocalDateTime nextExecution = computeNextExecution(settings, now());
            if (!sameDateTime(settings.getNextExecutionAt(), nextExecution)) {
                settings.setNextExecutionAt(nextExecution);
                persistSettings(settings);
            }
        }
    }

    public DatabaseBackupSummaryDTO getSummary() {
        synchronized (backupLock) {
            StoredSettings settings = loadSettings();
            LocalDateTime nextExecution = computeNextExecution(settings, now());
            if (!sameDateTime(settings.getNextExecutionAt(), nextExecution)) {
                settings.setNextExecutionAt(nextExecution);
                persistSettings(settings);
            }

            return new DatabaseBackupSummaryDTO(toSettingsDto(settings), listStoredBackups());
        }
    }

    public DatabaseBackupSettingsDTO updateSettings(DatabaseBackupSettingsDTO request) {
        synchronized (backupLock) {
            StoredSettings settings = loadSettings();
            applySettings(settings, request);
            settings.setNextExecutionAt(computeNextExecution(settings, now()));
            persistSettings(settings);
            return toSettingsDto(settings);
        }
    }

    public BackupFile exportDatabase() {
        synchronized (backupLock) {
            StoredBackup storedBackup = createBackup(BackupTrigger.MANUAL);
            updateBackupAudit(storedBackup, false);
            publishBackupNotification(storedBackup, false);
            return readBackupFile(storedBackup);
        }
    }

    public void restoreDatabase(MultipartFile backupFile) {
        if (backupFile == null || backupFile.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un archivo de respaldo.");
        }

        synchronized (backupLock) {
            ParsedJdbcUrl jdbc = ParsedJdbcUrl.parse(datasourceUrl);
            String originalName = backupFile.getOriginalFilename() == null ? "backup" : backupFile.getOriginalFilename();
            String lowerName = originalName.toLowerCase(Locale.ROOT);
            boolean plainSql = lowerName.endsWith(SQL_EXTENSION);
            Path tempFile = createTempFile(plainSql ? SQL_EXTENSION : BACKUP_EXTENSION);

            try {
                backupFile.transferTo(tempFile);
                executeRestore(tempFile, plainSql, jdbc);
                publishRestoreNotification(originalName);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo procesar el archivo de respaldo.", e);
            } finally {
                deleteQuietly(tempFile);
            }
        }
    }

    public void restoreStoredBackup(String filename) {
        synchronized (backupLock) {
            Path storedBackup = resolveStoredBackup(filename);
            executeRestore(
                    storedBackup,
                    storedBackup.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(SQL_EXTENSION),
                    ParsedJdbcUrl.parse(datasourceUrl));
            publishRestoreNotification(filename);
        }
    }

    public BackupFile downloadStoredBackup(String filename) {
        synchronized (backupLock) {
            return readBackupFile(toStoredBackup(resolveStoredBackup(filename)));
        }
    }

    /**
     * Revisa periódicamente si ya llegó la próxima hora programada para ejecutar
     * un respaldo automático. El poll se ejecuta cada minuto por defecto, pero
     * la fecha exacta se calcula con la configuración guardada por el administrador.
     */
    @Scheduled(fixedDelayString = "${application.database-backup.scheduler-delay-ms:60000}")
    public void executeScheduledBackup() {
        synchronized (backupLock) {
            StoredSettings settings = loadSettings();
            if (!settings.isEnabled()) {
                return;
            }

            LocalDateTime currentTime = now();
            LocalDateTime scheduledExecution = settings.getNextExecutionAt();
            if (scheduledExecution == null) {
                settings.setNextExecutionAt(computeNextExecution(settings, currentTime));
                persistSettings(settings);
                return;
            }

            if (currentTime.isBefore(scheduledExecution)) {
                return;
            }

            StoredBackup storedBackup = createBackup(BackupTrigger.AUTOMATIC);
            updateBackupAudit(storedBackup, true);
            publishBackupNotification(storedBackup, true);
        }
    }

    private void execute(List<String> command, ParsedJdbcUrl jdbc) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> environment = processBuilder.environment();
        environment.put("PGHOST", jdbc.host());
        environment.put("PGPORT", String.valueOf(jdbc.port()));
        environment.put("PGUSER", datasourceUsername);
        if (datasourcePassword != null && !datasourcePassword.isBlank()) {
            environment.put("PGPASSWORD", datasourcePassword);
        } else {
            environment.remove("PGPASSWORD");
        }
        processBuilder.redirectErrorStream(true);

        try {
            LOG.info("Ejecutando comando de respaldo/restauración PostgreSQL: {}", command.get(0));
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException(output.isBlank()
                        ? "La utilidad de PostgreSQL terminó con código " + exitCode + "."
                        : output.trim());
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo ejecutar la utilidad de PostgreSQL. Verifique que pg_dump/pg_restore/psql estén instalados.",
                    e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La operación de respaldo/restauración fue interrumpida.", e);
        }
    }

    private void executeRestore(Path backupFile, boolean plainSql, ParsedJdbcUrl jdbc) {
        List<String> command = new ArrayList<>();
        if (plainSql) {
            command.add("psql");
            command.add("-v");
            command.add("ON_ERROR_STOP=1");
            command.add("--file");
            command.add(backupFile.toString());
            command.add(jdbc.database());
        } else {
            command.add("pg_restore");
            command.add("--clean");
            command.add("--if-exists");
            command.add("--no-owner");
            command.add("--no-privileges");
            command.add("--dbname=" + jdbc.database());
            command.add(backupFile.toString());
        }
        execute(command, jdbc);
    }

    private Path createTempFile(String suffix) {
        try {
            return Files.createTempFile("control-expedientes-db-", suffix);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo preparar un archivo temporal para el respaldo.", e);
        }
    }

    private void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOG.warn("No se pudo eliminar el archivo temporal {}", file, e);
        }
    }

    private StoredBackup createBackup(BackupTrigger trigger) {
        ParsedJdbcUrl jdbc = ParsedJdbcUrl.parse(datasourceUrl);
        LocalDateTime timestamp = now();
        Path backupFile = backupDirectory().resolve("control-expedientes-" + trigger.slug + "-" + FILE_DATE.format(timestamp) + BACKUP_EXTENSION);

        List<String> command = List.of(
                "pg_dump",
                "--format=custom",
                "--clean",
                "--if-exists",
                "--no-owner",
                "--no-privileges",
                "--file",
                backupFile.toString(),
                jdbc.database());
        execute(command, jdbc);

        try {
            return new StoredBackup(
                    backupFile,
                    backupFile.getFileName().toString(),
                    timestamp,
                    Files.size(backupFile),
                    trigger);
        } catch (IOException e) {
            deleteQuietly(backupFile);
            throw new IllegalStateException("No se pudo completar el respaldo en el almacenamiento del servidor.", e);
        }
    }

    private BackupFile readBackupFile(StoredBackup storedBackup) {
        try {
            return new BackupFile(Files.readAllBytes(storedBackup.path()), storedBackup.filename(), "application/octet-stream");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo de respaldo almacenado.", e);
        }
    }

    private void updateBackupAudit(StoredBackup storedBackup, boolean automaticExecution) {
        StoredSettings settings = loadSettings();
        settings.setLastBackupAt(storedBackup.createdAt());
        settings.setLastBackupFilename(storedBackup.filename());
        if (automaticExecution) {
            settings.setLastAutomaticExecutionAt(storedBackup.createdAt());
        }
        settings.setNextExecutionAt(computeNextExecution(settings, storedBackup.createdAt().plusSeconds(1)));
        persistSettings(settings);
    }

    private void publishBackupNotification(StoredBackup storedBackup, boolean automaticExecution) {
        NotificacionDTO notification = new NotificacionDTO();
        notification.setTipo(automaticExecution ? "RESPALDO_AUTOMATICO" : "RESPALDO_MANUAL");
        notification.setMensaje(
            automaticExecution
                ? "Se generó un respaldo automático de la base de datos."
                : "Se generó un respaldo manual de la base de datos."
        );
        notification.setPacienteNombre("Sistema");
        notification.setRutaAccion("/admin/base-datos");
        notification.setArchivoDescarga(storedBackup.filename());
        notification.setAccionLabel("Descargar respaldo");
        notificacionService.notificarSistemaAdministrativo(notification);
    }

    private void publishRestoreNotification(String sourceName) {
        NotificacionDTO notification = new NotificacionDTO();
        notification.setTipo("RESTAURACION_BASE_DATOS");
        notification.setMensaje("Se restauró la base de datos usando " + sourceName + ".");
        notification.setPacienteNombre("Sistema");
        notification.setRutaAccion("/admin/base-datos");
        notification.setAccionLabel("Revisar historial");
        notificacionService.notificarSistemaAdministrativo(notification);
    }

    private void applySettings(StoredSettings settings, DatabaseBackupSettingsDTO request) {
        DatabaseBackupFrequency frequency = request.frequency() == null ? DatabaseBackupFrequency.DAILY : request.frequency();
        settings.setEnabled(request.enabled());
        settings.setFrequency(frequency);
        settings.setTime(request.time() == null ? LocalTime.of(2, 0) : request.time());
        settings.setDayOfWeek(request.dayOfWeek() == null ? DayOfWeek.MONDAY : request.dayOfWeek());
        settings.setIntervalHours(frequency == DatabaseBackupFrequency.INTERVAL_HOURS ? normalizeInterval(request.intervalHours()) : null);
    }

    private Integer normalizeInterval(Integer intervalHours) {
        if (intervalHours == null || intervalHours < 1) {
            return DEFAULT_INTERVAL_HOURS;
        }
        return Math.min(intervalHours, MAX_INTERVAL_HOURS);
    }

    private List<DatabaseBackupHistoryItemDTO> listStoredBackups() {
        ensureStorageDirectory();
        try (Stream<Path> files = Files.list(backupDirectory())) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String filename = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return filename.endsWith(BACKUP_EXTENSION) || filename.endsWith(SQL_EXTENSION);
                    })
                    .map(this::toStoredBackup)
                    .sorted(Comparator.comparing(StoredBackup::createdAt).reversed())
                    .map(item -> new DatabaseBackupHistoryItemDTO(
                            item.filename(),
                            item.sizeBytes(),
                            item.createdAt(),
                            item.trigger().label,
                            item.trigger() == BackupTrigger.AUTOMATIC))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el historial de respaldos almacenados.", e);
        }
    }

    private StoredBackup toStoredBackup(Path path) {
        try {
            String filename = path.getFileName().toString();
            LocalDateTime createdAt = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
            return new StoredBackup(path, filename, createdAt, Files.size(path), detectTrigger(filename));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el respaldo almacenado " + path.getFileName(), e);
        }
    }

    private BackupTrigger detectTrigger(String filename) {
        String normalized = filename.toLowerCase(Locale.ROOT);
        if (normalized.contains("-automatic-")) {
            return BackupTrigger.AUTOMATIC;
        }
        return BackupTrigger.MANUAL;
    }

    private DatabaseBackupSettingsDTO toSettingsDto(StoredSettings settings) {
        return new DatabaseBackupSettingsDTO(
                settings.isEnabled(),
                settings.getFrequency(),
                settings.getIntervalHours(),
                settings.getDayOfWeek(),
                settings.getTime(),
                settings.getLastBackupAt(),
                settings.getLastAutomaticExecutionAt(),
                settings.getNextExecutionAt(),
                settings.getLastBackupFilename());
    }

    /**
     * Calcula la próxima ejecución automática a partir de la configuración actual.
     * Para DAILY usa la siguiente ocurrencia de la hora configurada, para WEEKLY
     * usa el próximo día/hora indicado y para INTERVAL_HOURS parte de la última
     * ejecución automática registrada o de la primera hora configurada del día.
     */
    private LocalDateTime computeNextExecution(StoredSettings settings, LocalDateTime reference) {
        if (!settings.isEnabled()) {
            return null;
        }

        LocalTime time = settings.getTime() == null ? LocalTime.of(2, 0) : settings.getTime();
        DatabaseBackupFrequency frequency = settings.getFrequency() == null ? DatabaseBackupFrequency.DAILY : settings.getFrequency();

        return switch (frequency) {
            case DAILY -> {
                LocalDateTime candidate = reference.toLocalDate().atTime(time);
                yield !candidate.isBefore(reference) ? candidate : candidate.plusDays(1);
            }
            case WEEKLY -> {
                DayOfWeek dayOfWeek = settings.getDayOfWeek() == null ? DayOfWeek.MONDAY : settings.getDayOfWeek();
                LocalDate candidateDate = reference.toLocalDate();
                while (candidateDate.getDayOfWeek() != dayOfWeek || candidateDate.atTime(time).isBefore(reference)) {
                    candidateDate = candidateDate.plusDays(1);
                }
                yield candidateDate.atTime(time);
            }
            case INTERVAL_HOURS -> {
                int intervalHours = normalizeInterval(settings.getIntervalHours());
                LocalDateTime baseline = settings.getLastAutomaticExecutionAt();
                if (baseline == null) {
                    LocalDateTime firstExecution = reference.toLocalDate().atTime(time);
                    yield !firstExecution.isBefore(reference) ? firstExecution : firstExecution.plusHours(intervalHours);
                }

                LocalDateTime candidate = baseline.plusHours(intervalHours);
                while (candidate.isBefore(reference)) {
                    candidate = candidate.plusHours(intervalHours);
                }
                yield candidate;
            }
        };
    }

    private StoredSettings loadSettings() {
        Path settingsPath = settingsFilePath();
        ensureStorageDirectory();
        if (!Files.exists(settingsPath)) {
            return defaultSettings();
        }

        try {
            return objectMapper.readValue(settingsPath.toFile(), StoredSettings.class);
        } catch (IOException e) {
            LOG.warn("No se pudo leer la configuración persistida de respaldos. Se usarán valores por defecto.", e);
            return defaultSettings();
        }
    }

    private void persistSettings(StoredSettings settings) {
        ensureStorageDirectory();
        try {
            Path tempFile = Files.createTempFile(backupDirectory(), "backup-settings-", ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), settings);
            Files.move(tempFile, settingsFilePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la configuración de respaldos automáticos.", e);
        }
    }

    private StoredSettings defaultSettings() {
        StoredSettings settings = new StoredSettings();
        settings.setEnabled(false);
        settings.setFrequency(DatabaseBackupFrequency.DAILY);
        settings.setTime(LocalTime.of(2, 0));
        settings.setDayOfWeek(DayOfWeek.MONDAY);
        settings.setIntervalHours(DEFAULT_INTERVAL_HOURS);
        return settings;
    }

    private Path resolveStoredBackup(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el respaldo a utilizar.");
        }

        Path candidate = backupDirectory().resolve(filename).normalize();
        String normalizedFilename = candidate.getFileName().toString().toLowerCase(Locale.ROOT);
        boolean supportedExtension = normalizedFilename.endsWith(BACKUP_EXTENSION) || normalizedFilename.endsWith(SQL_EXTENSION);
        if (!candidate.startsWith(backupDirectory()) || !Files.isRegularFile(candidate) || !supportedExtension) {
            throw new IllegalArgumentException("No se encontró el respaldo solicitado.");
        }

        return candidate;
    }

    private Path backupDirectory() {
        return Path.of(backupStoragePath).toAbsolutePath().normalize();
    }

    private Path settingsFilePath() {
        return backupDirectory().resolve(SETTINGS_FILENAME);
    }

    private void ensureStorageDirectory() {
        try {
            Files.createDirectories(backupDirectory());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo preparar el directorio de respaldos del servidor.", e);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private boolean sameDateTime(LocalDateTime left, LocalDateTime right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.equals(right);
    }

    public record BackupFile(byte[] content, String filename, String contentType) {}

    private record StoredBackup(Path path, String filename, LocalDateTime createdAt, long sizeBytes, BackupTrigger trigger) {}

    private enum BackupTrigger {
        MANUAL("manual", "Manual"),
        AUTOMATIC("automatic", "Automático");

        private final String slug;
        private final String label;

        BackupTrigger(String slug, String label) {
            this.slug = slug;
            this.label = label;
        }
    }

    public static class StoredSettings {

        private boolean enabled;
        private DatabaseBackupFrequency frequency = DatabaseBackupFrequency.DAILY;
        private Integer intervalHours = DEFAULT_INTERVAL_HOURS;
        private DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        private LocalTime time = LocalTime.of(2, 0);
        private LocalDateTime lastBackupAt;
        private LocalDateTime lastAutomaticExecutionAt;
        private LocalDateTime nextExecutionAt;
        private String lastBackupFilename;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public DatabaseBackupFrequency getFrequency() {
            return frequency;
        }

        public void setFrequency(DatabaseBackupFrequency frequency) {
            this.frequency = frequency;
        }

        public Integer getIntervalHours() {
            return intervalHours;
        }

        public void setIntervalHours(Integer intervalHours) {
            this.intervalHours = intervalHours;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }

        public LocalDateTime getLastBackupAt() {
            return lastBackupAt;
        }

        public void setLastBackupAt(LocalDateTime lastBackupAt) {
            this.lastBackupAt = lastBackupAt;
        }

        public LocalDateTime getLastAutomaticExecutionAt() {
            return lastAutomaticExecutionAt;
        }

        public void setLastAutomaticExecutionAt(LocalDateTime lastAutomaticExecutionAt) {
            this.lastAutomaticExecutionAt = lastAutomaticExecutionAt;
        }

        public LocalDateTime getNextExecutionAt() {
            return nextExecutionAt;
        }

        public void setNextExecutionAt(LocalDateTime nextExecutionAt) {
            this.nextExecutionAt = nextExecutionAt;
        }

        public String getLastBackupFilename() {
            return lastBackupFilename;
        }

        public void setLastBackupFilename(String lastBackupFilename) {
            this.lastBackupFilename = lastBackupFilename;
        }
    }

    private record ParsedJdbcUrl(String host, int port, String database) {
        private static ParsedJdbcUrl parse(String jdbcUrl) {
            String prefix = "jdbc:postgresql://";
            if (jdbcUrl == null || !jdbcUrl.startsWith(prefix)) {
                throw new IllegalStateException("La URL de base de datos no es PostgreSQL: " + jdbcUrl);
            }

            String remaining = jdbcUrl.substring(prefix.length());
            String withoutParams = remaining.split("\\?", 2)[0];
            String[] parts = withoutParams.split("/", 2);
            if (parts.length != 2) {
                throw new IllegalStateException("No se pudo determinar la base de datos desde la URL JDBC.");
            }

            String hostPort = parts[0];
            String database = parts[1];
            String[] hostParts = hostPort.split(":", 2);
            String host = hostParts[0];
            int port = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 5432;
            return new ParsedJdbcUrl(host, port, database);
        }
    }
}
