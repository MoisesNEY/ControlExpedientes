package ni.edu.mney.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import ni.edu.mney.domain.AuditoriaAcciones;
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Diagnostico;
import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.enumeration.EstadoCita;
import ni.edu.mney.domain.enumeration.Sexo;
import ni.edu.mney.repository.AuditoriaAccionesRepository;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.repository.DiagnosticoRepository;
import ni.edu.mney.repository.MedicamentoRepository;
import ni.edu.mney.repository.PacienteRepository;
import ni.edu.mney.security.SecurityUtils;
import ni.edu.mney.service.dto.DashboardMetricsDTO;
import ni.edu.mney.service.dto.DashboardMetricsDTO.ActivityItemDTO;
import ni.edu.mney.service.dto.DashboardMetricsDTO.ListItemDTO;
import ni.edu.mney.service.dto.DashboardMetricsDTO.MetricCardDTO;
import ni.edu.mney.service.dto.DashboardMetricsDTO.SeriesPointDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Locale LOCALE = new Locale("es", "NI");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaMedicaRepository citaMedicaRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AuditoriaAccionesRepository auditoriaAccionesRepository;

    public DashboardService(
        CitaMedicaRepository citaMedicaRepository,
        ConsultaMedicaRepository consultaMedicaRepository,
        DiagnosticoRepository diagnosticoRepository,
        PacienteRepository pacienteRepository,
        MedicamentoRepository medicamentoRepository,
        AuditoriaAccionesRepository auditoriaAccionesRepository
    ) {
        this.citaMedicaRepository = citaMedicaRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.auditoriaAccionesRepository = auditoriaAccionesRepository;
    }

    public DashboardMetricsDTO getAdminMetrics() {
        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        ZonedDateTime startOfToday = startOfToday();
        ZonedDateTime endOfToday = endOfToday();
        ZonedDateTime startOfWeek = startOfToday.minusDays(6);
        LocalDate weekStartDate = startOfToday.toLocalDate().minusDays(6);
        LocalDate today = startOfToday.toLocalDate();

        List<CitaMedica> citasHoy = citaMedicaRepository.findAllByFechaHoraBetween(startOfToday, endOfToday);
        List<CitaMedica> citasSemana = citaMedicaRepository.findAllByFechaHoraBetween(startOfWeek, endOfToday);
        List<ConsultaMedica> consultasSemana = consultaMedicaRepository.findAllByFechaConsultaBetween(weekStartDate, today);
        List<Paciente> pacientes = pacienteRepository.findAll();
        List<Medicamento> lowStock = medicamentoRepository.findByStockLessThan(10)
            .stream()
            .sorted(Comparator.comparing(Medicamento::getStock))
            .limit(6)
            .toList();

        long pacientesActivos = pacientes.stream().filter(p -> Boolean.TRUE.equals(p.getActivo())).count();

        dto.setCards(List.of(
            new MetricCardDTO("pacientesActivos", "Pacientes activos", pacientesActivos, "Base clínica vigente"),
            new MetricCardDTO("citasHoy", "Citas hoy", (long) citasHoy.size(), "Agenda del día"),
            new MetricCardDTO("consultasSemana", "Consultas 7 días", (long) consultasSemana.size(), "Seguimiento clínico"),
            new MetricCardDTO("stockBajo", "Medicamentos críticos", (long) lowStock.size(), "Stock menor a 10")
        ));

        dto.setPrimarySeries(buildWeeklyAppointmentSeries(citasSemana, startOfWeek.toLocalDate()));
        dto.setSecondarySeries(buildStatusSeries(citasHoy, Arrays.asList(
            EstadoCita.PROGRAMADA,
            EstadoCita.EN_SALA_ESPERA,
            EstadoCita.EN_TRIAGE,
            EstadoCita.ESPERANDO_MEDICO,
            EstadoCita.EN_CONSULTA,
            EstadoCita.ATENDIDA,
            EstadoCita.CANCELADA
        )));
        dto.setTertiarySeries(buildSexDistributionSeries(pacientes));
        dto.setQueue(lowStock.stream().map(medicamento -> new ListItemDTO(
            medicamento.getId(),
            medicamento.getNombre(),
            medicamento.getDescripcion() == null || medicamento.getDescripcion().isBlank() ? "Sin descripción" : medicamento.getDescripcion(),
            medicamento.getStock() <= 3 ? "CRITICO" : "BAJO",
            null,
            "Stock: " + medicamento.getStock()
        )).toList());
        dto.setActivity(
            auditoriaAccionesRepository.findAll(PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "fecha"))).getContent().stream()
                .map(this::toActivityItem)
                .toList()
        );
        return dto;
    }

    public DashboardMetricsDTO getDoctorMetrics() {
        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        String login = getCurrentLogin();
        ZonedDateTime startOfToday = startOfToday();
        ZonedDateTime endOfToday = endOfToday();
        LocalDate today = startOfToday.toLocalDate();
        LocalDate weekStart = today.minusDays(6);
        LocalDate diagnosisStart = today.minusDays(29);

        List<CitaMedica> citasHoy = citaMedicaRepository.findAllByFechaHoraBetweenAndUserLogin(startOfToday, endOfToday, login);
        List<ConsultaMedica> consultasSemana = consultaMedicaRepository.findAllByFechaConsultaBetweenAndUserLogin(weekStart, today, login);
        List<Diagnostico> diagnosticos = diagnosticoRepository.findAllByConsultaFechaConsultaBetweenAndConsultaUserLogin(diagnosisStart, today, login);
        List<CitaMedica> cola = citaMedicaRepository.findTop6ByUserLoginAndEstadoOrderByFechaHoraAsc(login, EstadoCita.ESPERANDO_MEDICO);
        CitaMedica activeConsultation = citaMedicaRepository.findFirstByUserLoginAndEstadoOrderByFechaHoraDesc(login, EstadoCita.EN_CONSULTA).orElse(null);

        long waitingToday = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.ESPERANDO_MEDICO).count();
        long attendedToday = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count();
        long activeToday = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.EN_CONSULTA).count();

        dto.setCards(List.of(
            new MetricCardDTO("totalAsignadas", "Citas asignadas hoy", (long) citasHoy.size(), "Agenda del médico"),
            new MetricCardDTO("enEspera", "Pacientes listos", waitingToday, "Esperando consulta"),
            new MetricCardDTO("atendidas", "Atendidas hoy", attendedToday, "Consultas finalizadas"),
            new MetricCardDTO("activas", "Consultas activas", activeToday, "Sesiones en curso")
        ));
        dto.setPrimarySeries(buildWeeklyConsultationSeries(consultasSemana, weekStart));
        dto.setSecondarySeries(buildStatusSeries(citasHoy, Arrays.asList(
            EstadoCita.ESPERANDO_MEDICO,
            EstadoCita.EN_CONSULTA,
            EstadoCita.ATENDIDA,
            EstadoCita.CANCELADA,
            EstadoCita.PROGRAMADA
        )));
        dto.setTertiarySeries(
            diagnosticos.stream()
                .collect(Collectors.groupingBy(Diagnostico::getDescripcion, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new SeriesPointDTO(entry.getKey(), entry.getValue()))
                .toList()
        );
        dto.setQueue(cola.stream().map(this::toAppointmentListItem).toList());
        if (activeConsultation != null) {
            dto.setSpotlight(toAppointmentListItem(activeConsultation));
        }
        return dto;
    }

    public DashboardMetricsDTO getNurseMetrics() {
        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        ZonedDateTime startOfToday = startOfToday();
        ZonedDateTime endOfToday = endOfToday();
        List<CitaMedica> citasHoy = citaMedicaRepository.findAllByFechaHoraBetween(startOfToday, endOfToday);

        long waitingRoom = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.EN_SALA_ESPERA).count();
        long triageInProgress = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.EN_TRIAGE).count();
        long readyForDoctor = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.ESPERANDO_MEDICO).count();
        long scheduled = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count();

        dto.setCards(List.of(
            new MetricCardDTO("salaEspera", "En sala de espera", waitingRoom, "Pendientes de triage"),
            new MetricCardDTO("triage", "Triage en curso", triageInProgress, "Pacientes siendo evaluados"),
            new MetricCardDTO("listosMedico", "Listos para médico", readyForDoctor, "Post triage"),
            new MetricCardDTO("programadas", "Aún programadas", scheduled, "Pendientes de check-in")
        ));
        dto.setPrimarySeries(buildHourlySeries(citasHoy, cita -> cita.getFechaHora().getHour()));
        dto.setSecondarySeries(buildStatusSeries(citasHoy, Arrays.asList(
            EstadoCita.EN_SALA_ESPERA,
            EstadoCita.EN_TRIAGE,
            EstadoCita.ESPERANDO_MEDICO,
            EstadoCita.EN_CONSULTA,
            EstadoCita.ATENDIDA
        )));
        dto.setTertiarySeries(buildHourlySeries(
            citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.ESPERANDO_MEDICO || c.getEstado() == EstadoCita.EN_CONSULTA || c.getEstado() == EstadoCita.ATENDIDA).toList(),
            cita -> cita.getFechaHora().getHour()
        ));
        dto.setQueue(
            citasHoy.stream()
                .filter(c -> c.getEstado() == EstadoCita.EN_SALA_ESPERA)
                .sorted(Comparator.comparing(CitaMedica::getFechaHora))
                .limit(8)
                .map(this::toAppointmentListItem)
                .toList()
        );
        if (!dto.getQueue().isEmpty()) {
            dto.setSpotlight(dto.getQueue().get(0));
        }
        return dto;
    }

    public DashboardMetricsDTO getReceptionMetrics() {
        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        ZonedDateTime startOfToday = startOfToday();
        ZonedDateTime endOfToday = endOfToday();
        List<CitaMedica> citasHoy = citaMedicaRepository.findAllByFechaHoraBetween(startOfToday, endOfToday);
        long activePatients = pacienteRepository.findAll().stream().filter(p -> Boolean.TRUE.equals(p.getActivo())).count();

        long checkedIn = citasHoy.stream().filter(c -> Arrays.asList(
            EstadoCita.EN_SALA_ESPERA,
            EstadoCita.EN_TRIAGE,
            EstadoCita.ESPERANDO_MEDICO,
            EstadoCita.EN_CONSULTA,
            EstadoCita.ATENDIDA
        ).contains(c.getEstado())).count();
        long inAttention = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.EN_TRIAGE || c.getEstado() == EstadoCita.ESPERANDO_MEDICO || c.getEstado() == EstadoCita.EN_CONSULTA).count();
        long cancelled = citasHoy.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();

        dto.setCards(List.of(
            new MetricCardDTO("citasHoy", "Citas hoy", (long) citasHoy.size(), "Agenda diaria"),
            new MetricCardDTO("checkIn", "Pacientes registrados", checkedIn, "Check-in completado"),
            new MetricCardDTO("enAtencion", "En flujo clínico", inAttention, "Desde triage hasta consulta"),
            new MetricCardDTO("pacientesActivos", "Pacientes activos", activePatients, "Base habilitada")
        ));
        dto.setPrimarySeries(buildHourlySeries(citasHoy, cita -> cita.getFechaHora().getHour()));
        dto.setSecondarySeries(buildStatusSeries(citasHoy, Arrays.asList(
            EstadoCita.PROGRAMADA,
            EstadoCita.EN_SALA_ESPERA,
            EstadoCita.EN_TRIAGE,
            EstadoCita.ESPERANDO_MEDICO,
            EstadoCita.EN_CONSULTA,
            EstadoCita.ATENDIDA,
            EstadoCita.CANCELADA
        )));
        dto.setTertiarySeries(
            citasHoy.stream()
                .collect(Collectors.groupingBy(
                    cita -> cita.getUser() != null && cita.getUser().getLogin() != null ? cita.getUser().getLogin() : "Sin asignar",
                    LinkedHashMap::new,
                    Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> new SeriesPointDTO(entry.getKey(), entry.getValue()))
                .toList()
        );
        dto.setQueue(
            citaMedicaRepository.findTop8ByFechaHoraBetweenAndEstadoNotInOrderByFechaHoraAsc(
                startOfToday,
                endOfToday,
                List.of(EstadoCita.ATENDIDA, EstadoCita.CANCELADA)
            ).stream().map(this::toAppointmentListItem).toList()
        );
        dto.setSpotlight(cancelled > 0 ? new ListItemDTO(null, "Citas canceladas hoy", "Revisar reprogramaciones", "CANCELADA", null, String.valueOf(cancelled)) : null);
        return dto;
    }

    private String getCurrentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    private ZonedDateTime startOfToday() {
        return LocalDate.now(ZONE).atStartOfDay(ZONE);
    }

    private ZonedDateTime endOfToday() {
        return LocalDate.now(ZONE).atTime(LocalTime.MAX).atZone(ZONE);
    }

    private List<SeriesPointDTO> buildWeeklyAppointmentSeries(List<CitaMedica> citas, LocalDate startDate) {
        Map<LocalDate, List<CitaMedica>> grouped = citas.stream().collect(Collectors.groupingBy(cita -> cita.getFechaHora().withZoneSameInstant(ZONE).toLocalDate()));
        return buildDaySeries(startDate, 7, day -> {
            List<CitaMedica> dayAppointments = grouped.getOrDefault(day, List.of());
            long attended = dayAppointments.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count();
            long cancelled = dayAppointments.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
            return new SeriesPointDTO(shortDayLabel(day), (long) dayAppointments.size(), attended, cancelled);
        });
    }

    private List<SeriesPointDTO> buildWeeklyConsultationSeries(List<ConsultaMedica> consultas, LocalDate startDate) {
        Map<LocalDate, Long> grouped = consultas.stream()
            .collect(Collectors.groupingBy(ConsultaMedica::getFechaConsulta, Collectors.counting()));
        return buildDaySeries(startDate, 7, day -> new SeriesPointDTO(shortDayLabel(day), grouped.getOrDefault(day, 0L)));
    }

    private List<SeriesPointDTO> buildSexDistributionSeries(List<Paciente> pacientes) {
        Map<Sexo, Long> grouped = pacientes.stream()
            .filter(p -> p.getSexo() != null)
            .collect(Collectors.groupingBy(Paciente::getSexo, () -> new EnumMap<>(Sexo.class), Collectors.counting()));

        return Arrays.stream(Sexo.values())
            .map(sexo -> new SeriesPointDTO(capitalize(sexo.name()), grouped.getOrDefault(sexo, 0L)))
            .toList();
    }

    private List<SeriesPointDTO> buildStatusSeries(List<CitaMedica> citas, Collection<EstadoCita> orderedStatuses) {
        Map<EstadoCita, Long> grouped = citas.stream()
            .filter(cita -> cita.getEstado() != null)
            .collect(Collectors.groupingBy(CitaMedica::getEstado, () -> new EnumMap<>(EstadoCita.class), Collectors.counting()));

        return orderedStatuses.stream()
            .map(status -> new SeriesPointDTO(statusLabel(status), grouped.getOrDefault(status, 0L)))
            .filter(point -> point.getValue() > 0)
            .toList();
    }

    private List<SeriesPointDTO> buildHourlySeries(List<CitaMedica> citas, Function<CitaMedica, Integer> hourExtractor) {
        Map<Integer, Long> grouped = citas.stream().collect(Collectors.groupingBy(hourExtractor, TreeMapSupplier::new, Collectors.counting()));
        return grouped.entrySet().stream()
            .map(entry -> new SeriesPointDTO(String.format("%02d:00", entry.getKey()), entry.getValue()))
            .toList();
    }

    private List<ListItemDTO> toQueueItems(List<CitaMedica> citas) {
        return citas.stream().map(this::toAppointmentListItem).toList();
    }

    private ListItemDTO toAppointmentListItem(CitaMedica cita) {
        String patientName = cita.getPaciente() != null
            ? String.format("%s %s", defaultText(cita.getPaciente().getNombres()), defaultText(cita.getPaciente().getApellidos())).trim()
            : "Paciente no identificado";

        String doctor = cita.getUser() != null && cita.getUser().getLogin() != null ? cita.getUser().getLogin() : "Sin asignar";
        String meta = cita.getObservaciones() != null && !cita.getObservaciones().isBlank() ? cita.getObservaciones() : doctor;

        return new ListItemDTO(
            cita.getId(),
            patientName,
            doctor,
            cita.getEstado() != null ? cita.getEstado().name() : "SIN_ESTADO",
            cita.getFechaHora() != null ? cita.getFechaHora().format(TIMESTAMP_FORMAT) : null,
            meta
        );
    }

    private ActivityItemDTO toActivityItem(AuditoriaAcciones audit) {
        return new ActivityItemDTO(
            audit.getId(),
            String.format("%s - %s", defaultText(audit.getAccion()), defaultText(audit.getEntidad())),
            audit.getDescripcion() == null || audit.getDescripcion().isBlank() ? "Sin descripción" : audit.getDescripcion(),
            audit.getFecha() != null ? audit.getFecha().format(TIMESTAMP_FORMAT) : null,
            audit.getAccion()
        );
    }

    private List<SeriesPointDTO> buildDaySeries(LocalDate startDate, int days, Function<LocalDate, SeriesPointDTO> mapper) {
        return java.util.stream.IntStream.range(0, days)
            .mapToObj(startDate::plusDays)
            .map(mapper)
            .toList();
    }

    private String shortDayLabel(LocalDate day) {
        return day.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE);
    }

    private String statusLabel(EstadoCita status) {
        return switch (status) {
            case PROGRAMADA -> "Programada";
            case EN_SALA_ESPERA -> "Recepción";
            case EN_TRIAGE -> "Triage";
            case ESPERANDO_MEDICO -> "Espera médico";
            case EN_CONSULTA -> "Consulta";
            case ATENDIDA -> "Atendida";
            case CANCELADA -> "Cancelada";
        };
    }

    private String capitalize(String value) {
        String normalized = value.toLowerCase(LOCALE).replace('_', ' ');
        return normalized.substring(0, 1).toUpperCase(LOCALE) + normalized.substring(1);
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private static class TreeMapSupplier extends java.util.TreeMap<Integer, Long> {
        private static final long serialVersionUID = 1L;
    }
}