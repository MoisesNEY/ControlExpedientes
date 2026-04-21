package ni.edu.mney.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DatabaseBackupService {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseBackupService.class);
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    public BackupFile exportDatabase() {
        ParsedJdbcUrl jdbc = ParsedJdbcUrl.parse(datasourceUrl);
        Path backupFile = createTempFile(".backup");
        try {
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
            return new BackupFile(
                    Files.readAllBytes(backupFile),
                    "control-expedientes-backup-" + FILE_DATE.format(LocalDateTime.now()) + ".backup",
                    "application/octet-stream");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el archivo de respaldo.", e);
        } finally {
            deleteQuietly(backupFile);
        }
    }

    public void restoreDatabase(MultipartFile backupFile) {
        if (backupFile == null || backupFile.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un archivo de respaldo.");
        }

        ParsedJdbcUrl jdbc = ParsedJdbcUrl.parse(datasourceUrl);
        String originalName = backupFile.getOriginalFilename() == null ? "backup" : backupFile.getOriginalFilename();
        String lowerName = originalName.toLowerCase(Locale.ROOT);
        boolean plainSql = lowerName.endsWith(".sql");
        Path tempFile = createTempFile(plainSql ? ".sql" : ".backup");

        try {
            backupFile.transferTo(tempFile);
            List<String> command = new ArrayList<>();
            if (plainSql) {
                command.add("psql");
                command.add("-v");
                command.add("ON_ERROR_STOP=1");
                command.add("--file");
                command.add(tempFile.toString());
                command.add(jdbc.database());
            } else {
                command.add("pg_restore");
                command.add("--clean");
                command.add("--if-exists");
                command.add("--no-owner");
                command.add("--no-privileges");
                command.add("--dbname=" + jdbc.database());
                command.add(tempFile.toString());
            }
            execute(command, jdbc);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo procesar el archivo de respaldo.", e);
        } finally {
            deleteQuietly(tempFile);
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
            LOG.info("Ejecutando comando de respaldo/restauración PostgreSQL: {}", command.getFirst());
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

    public record BackupFile(byte[] content, String filename, String contentType) {}

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
