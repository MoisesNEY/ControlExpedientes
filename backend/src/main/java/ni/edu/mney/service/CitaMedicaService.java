package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.SignosVitales;
import ni.edu.mney.domain.enumeration.EstadoCita;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.service.dto.CitaMedicaDTO;
import ni.edu.mney.service.dto.CitaTriageDTO;
import ni.edu.mney.service.dto.NotificacionDTO;
import ni.edu.mney.service.mapper.CitaMedicaMapper;
import ni.edu.mney.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.CitaMedica}.
 */
@Service
@Transactional
public class CitaMedicaService {

    private static final Logger LOG = LoggerFactory.getLogger(CitaMedicaService.class);

    private final CitaMedicaRepository citaMedicaRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final CitaMedicaMapper citaMedicaMapper;
    private final NotificacionService notificacionService;

    public CitaMedicaService(
        CitaMedicaRepository citaMedicaRepository,
        ConsultaMedicaRepository consultaMedicaRepository, 
        CitaMedicaMapper citaMedicaMapper,
        NotificacionService notificacionService
    ) {
        this.citaMedicaRepository = citaMedicaRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.citaMedicaMapper = citaMedicaMapper;
        this.notificacionService = notificacionService;
    }

    /**
     * Save a citaMedica.
     *
     * @param citaMedicaDTO the entity to save.
     * @return the persisted entity.
     */
    public CitaMedicaDTO save(CitaMedicaDTO citaMedicaDTO) {
        LOG.debug("Request to save CitaMedica : {}", citaMedicaDTO);
        CitaMedica citaMedica = citaMedicaMapper.toEntity(citaMedicaDTO);
        citaMedica = citaMedicaRepository.save(citaMedica);
        citaMedicaRepository.findWithNotificationDetailsById(citaMedica.getId()).ifPresent(this::notifyAppointmentCreated);
        return citaMedicaMapper.toDto(citaMedica);
    }

    /**
     * Update a citaMedica.
     *
     * @param citaMedicaDTO the entity to save.
     * @return the persisted entity.
     */
    public CitaMedicaDTO update(CitaMedicaDTO citaMedicaDTO) {
        LOG.debug("Request to update CitaMedica : {}", citaMedicaDTO);
        CitaMedica previous = citaMedicaRepository.findWithNotificationDetailsById(citaMedicaDTO.getId()).orElse(null);
        CitaMedica citaMedica = citaMedicaMapper.toEntity(citaMedicaDTO);
        citaMedica = citaMedicaRepository.save(citaMedica);
        CitaMedica current = citaMedicaRepository.findWithNotificationDetailsById(citaMedica.getId()).orElse(citaMedica);
        notifyAppointmentUpdated(previous, current);
        return citaMedicaMapper.toDto(citaMedica);
    }

    /**
     * Partially update a citaMedica.
     *
     * @param citaMedicaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CitaMedicaDTO> partialUpdate(CitaMedicaDTO citaMedicaDTO) {
        LOG.debug("Request to partially update CitaMedica : {}", citaMedicaDTO);

        return citaMedicaRepository
            .findById(citaMedicaDTO.getId())
            .map(existingCitaMedica -> {
                EstadoCita estadoAnterior = existingCitaMedica.getEstado();
                citaMedicaMapper.partialUpdate(existingCitaMedica, citaMedicaDTO);
                notifyStateTransition(existingCitaMedica, estadoAnterior, existingCitaMedica.getEstado());

                return existingCitaMedica;
            })
            .map(citaMedicaRepository::save)
            .map(citaMedicaMapper::toDto);
    }

    /**
     * Get all the citaMedicas with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CitaMedicaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return citaMedicaRepository.findAllWithEagerRelationships(pageable).map(citaMedicaMapper::toDto);
    }

    /**
     * Get one citaMedica by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CitaMedicaDTO> findOne(Long id) {
        LOG.debug("Request to get CitaMedica : {}", id);
        return citaMedicaRepository.findOneWithEagerRelationships(id).map(citaMedicaMapper::toDto);
    }

    /**
     * Delete the citaMedica by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete CitaMedica : {}", id);
        citaMedicaRepository.findWithNotificationDetailsById(id).ifPresent(this::notifyAppointmentDeleted);
        citaMedicaRepository.deleteById(id);
    }

    /**
     * Start a medical consultation safely using state machine logic.
     * Assembles a cross-entity DTO spanning `CitaMedica`, `Paciente` and `SignosVitales`.
     *
     * @param citaId the id of the appointment.
     * @return the unified CitaTriageDTO.
     */
    @Transactional
    public CitaTriageDTO iniciarConsulta(Long citaId) {
        LOG.debug("Request to start consultation for CitaMedica : {}", citaId);
        
        CitaMedica cita = citaMedicaRepository.findWithPacienteAndExpedienteById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("CitaMedica con ID " + citaId + " no encontrada"));
                
        // Validation (State Machine Guard)
        if (cita.getEstado() != EstadoCita.ESPERANDO_MEDICO && cita.getEstado() != EstadoCita.EN_TRIAGE) {
            throw new IllegalStateException("Transición inválida: El paciente debe haber pasado por Triage o estar Esperando al Médico. Estado actual: " + cita.getEstado());
        }

        // Change state to EN_CONSULTA
        cita.setEstado(EstadoCita.EN_CONSULTA);
        citaMedicaRepository.save(cita);
        notifyStateTransition(cita, EstadoCita.ESPERANDO_MEDICO, EstadoCita.EN_CONSULTA);

        // Manual Data Assembling mapped towards Clean Architecture
        CitaTriageDTO dto = new CitaTriageDTO();
        dto.setCitaId(cita.getId());
        dto.setFechaHoraCita(cita.getFechaHora());
        dto.setEstadoCita(cita.getEstado());
        
        Paciente p = cita.getPaciente();
        if (p != null) {
            dto.setPacienteId(p.getId());
            dto.setPacienteNombreCompleto(p.getNombres() + " " + p.getApellidos());
            
            if (p.getExpediente() != null) {
                // Fetch the attached Triage (ConsultaMedica) for this Patient on this Date
                Optional<ConsultaMedica> consultaOpt = consultaMedicaRepository
                    .findFirstByExpedienteIdAndFechaConsultaOrderByIdDesc(p.getExpediente().getId(), cita.getFechaHora().toLocalDate());
                    
                if (consultaOpt.isPresent()) {
                    ConsultaMedica cm = consultaOpt.get();
                    dto.setConsultaId(cm.getId());
                    dto.setMotivoConsulta(cm.getMotivoConsulta());
                    
                    if (cm.getSignosVitales() != null && !cm.getSignosVitales().isEmpty()) {
                        SignosVitales sv = cm.getSignosVitales().iterator().next(); // Toma los ultimos parametros
                        dto.setSignosVitalesId(sv.getId());
                        dto.setPeso(sv.getPeso());
                        dto.setPresionArterial(sv.getPresionArterial());
                        dto.setTemperatura(sv.getTemperatura());
                    }
                }
            }
        }
        
        return dto;
    }

    private void notifyAppointmentCreated(CitaMedica cita) {
        NotificacionDTO notification = baseNotification(
            "CITA_AGENDADA",
            patientName(cita) + " tiene una cita agendada para " + formatAppointmentDate(cita),
            cita,
            "Ver agenda",
            "/medico/citas"
        );
        notifyAssignedDoctor(cita, notification);
        if (cita.getEstado() == EstadoCita.EN_SALA_ESPERA) {
            notifyStateTransition(cita, null, EstadoCita.EN_SALA_ESPERA);
        }
    }

    private void notifyAppointmentUpdated(CitaMedica previous, CitaMedica current) {
        if (previous == null) {
            return;
        }
        if (previous.getEstado() != current.getEstado()) {
            notifyStateTransition(current, previous.getEstado(), current.getEstado());
            return;
        }
        boolean doctorChanged = login(previous) != null && !login(previous).equals(login(current));
        boolean dateChanged = previous.getFechaHora() != null && !previous.getFechaHora().equals(current.getFechaHora());
        if (doctorChanged || dateChanged) {
            NotificacionDTO notification = baseNotification(
                "CITA_REPROGRAMADA",
                patientName(current) + " tiene cambios en su cita. Nueva hora: " + formatAppointmentDate(current),
                current,
                "Ver agenda",
                "/medico/citas"
            );
            notifyAssignedDoctor(current, notification);
            if (doctorChanged) {
                NotificacionDTO previousDoctorNotification = baseNotification(
                    "CITA_REASIGNADA",
                    "La cita de " + patientName(current) + " fue reasignada a otro médico.",
                    current,
                    "Ver agenda",
                    "/medico/citas"
                );
                notificacionService.notificarUsuario(login(previous), previousDoctorNotification);
            }
        }
    }

    private void notifyAppointmentDeleted(CitaMedica cita) {
        NotificacionDTO notification = baseNotification(
            "CITA_ELIMINADA",
            "La cita de " + patientName(cita) + " fue eliminada de la agenda.",
            cita,
            "Ver agenda",
            "/medico/citas"
        );
        notifyAssignedDoctor(cita, notification);
        notificacionService.notificarRoles(
            java.util.List.of(AuthoritiesConstants.RECEPCION, AuthoritiesConstants.ADMIN),
            notification,
            true
        );
    }

    private void notifyStateTransition(CitaMedica cita, EstadoCita previous, EstadoCita current) {
        if (current == null || current == previous) {
            return;
        }
        switch (current) {
            case EN_SALA_ESPERA -> {
                NotificacionDTO notification = baseNotification(
                    "PACIENTE_EN_SALA",
                    patientName(cita) + " llegó y está esperando triage.",
                    cita,
                    "Abrir sala",
                    "/enfermeria/sala-espera"
                );
                notificacionService.notificarRoles(java.util.List.of(AuthoritiesConstants.ENFERMERO), notification, true);
                notifyAssignedDoctor(cita, baseNotification(
                    "PACIENTE_LLEGO",
                    patientName(cita) + " ya hizo check-in en recepción.",
                    cita,
                    "Ver agenda",
                    "/medico/citas"
                ));
            }
            case EN_TRIAGE -> {
                NotificacionDTO notification = baseNotification(
                    "TRIAGE_INICIADO",
                    "Triage iniciado para " + patientName(cita) + ".",
                    cita,
                    "Abrir sala",
                    "/enfermeria/sala-espera"
                );
                notificacionService.notificarRoles(
                    java.util.List.of(AuthoritiesConstants.RECEPCION, AuthoritiesConstants.ADMIN),
                    notification,
                    true
                );
            }
            case ESPERANDO_MEDICO -> {
                NotificacionDTO notification = baseNotification(
                    "PACIENTE_LISTO",
                    patientName(cita) + " está listo para atención médica.",
                    cita,
                    "Iniciar consulta",
                    "/medico/consulta/" + cita.getId()
                );
                notificacionService.notificarPacienteListo(notification);
                notifyAssignedDoctor(cita, notification);
                notificacionService.notificarRoles(java.util.List.of(AuthoritiesConstants.RECEPCION), baseNotification(
                    "TRIAGE_FINALIZADO",
                    patientName(cita) + " finalizó triage y espera al médico.",
                    cita,
                    "Ver agenda",
                    "/recepcion/citas"
                ), true);
            }
            case EN_CONSULTA -> {
                NotificacionDTO notification = baseNotification(
                    "CONSULTA_INICIADA",
                    "El médico inició la consulta de " + patientName(cita) + ".",
                    cita,
                    "Ver sala",
                    "/enfermeria/sala-espera"
                );
                notificacionService.notificarRoles(
                    java.util.List.of(AuthoritiesConstants.ENFERMERO, AuthoritiesConstants.RECEPCION),
                    notification,
                    true
                );
            }
            case ATENDIDA -> notifyAppointmentCompleted(cita);
            case CANCELADA -> {
                NotificacionDTO notification = baseNotification(
                    "CITA_CANCELADA",
                    "La cita de " + patientName(cita) + " fue cancelada.",
                    cita,
                    "Ver agenda",
                    "/medico/citas"
                );
                notifyAssignedDoctor(cita, notification);
                notificacionService.notificarRoles(
                    java.util.List.of(AuthoritiesConstants.ENFERMERO, AuthoritiesConstants.RECEPCION, AuthoritiesConstants.ADMIN),
                    notification,
                    true
                );
            }
            default -> {
            }
        }
    }

    public void notifyAppointmentCompleted(CitaMedica cita) {
        NotificacionDTO notification = baseNotification(
            "CONSULTA_FINALIZADA",
            "La consulta de " + patientName(cita) + " fue finalizada.",
            cita,
            "Ver reportes",
            "/reportes"
        );
        notificacionService.notificarRoles(
            java.util.List.of(AuthoritiesConstants.RECEPCION, AuthoritiesConstants.ENFERMERO, AuthoritiesConstants.ADMIN),
            notification,
            true
        );
    }

    private void notifyAssignedDoctor(CitaMedica cita, NotificacionDTO notification) {
        String login = login(cita);
        if (login == null || login.isBlank()) {
            return;
        }
        notification.setMedicoLogin(login);
        notificacionService.notificarMedicoEspecifico(login, notification);
    }

    private NotificacionDTO baseNotification(String type, String message, CitaMedica cita, String actionLabel, String route) {
        NotificacionDTO notification = new NotificacionDTO(type, message, cita.getId(), patientName(cita));
        notification.setMedicoLogin(login(cita));
        notification.setAccionLabel(actionLabel);
        notification.setRutaAccion(route);
        return notification;
    }

    private String patientName(CitaMedica cita) {
        if (cita == null || cita.getPaciente() == null) {
            return "Paciente";
        }
        String nombres = cita.getPaciente().getNombres() != null ? cita.getPaciente().getNombres() : "";
        String apellidos = cita.getPaciente().getApellidos() != null ? cita.getPaciente().getApellidos() : "";
        String fullName = (nombres + " " + apellidos).trim();
        return fullName.isBlank() ? "Paciente" : fullName;
    }

    private String login(CitaMedica cita) {
        return cita != null && cita.getUser() != null ? cita.getUser().getLogin() : null;
    }

    private String formatAppointmentDate(CitaMedica cita) {
        return cita.getFechaHora() != null
            ? cita.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            : "fecha pendiente";
    }
}
