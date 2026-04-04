package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.SignosVitales;
import ni.edu.mney.domain.enumeration.EstadoCita;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.service.dto.CitaMedicaDTO;
import ni.edu.mney.service.dto.CitaTriageDTO;
import ni.edu.mney.service.dto.NotificacionDTO;
import ni.edu.mney.service.mapper.CitaMedicaMapper;
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
        CitaMedica citaMedica = citaMedicaMapper.toEntity(citaMedicaDTO);
        citaMedica = citaMedicaRepository.save(citaMedica);
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

                // Si transiciona a ESPERANDO_MEDICO, enviar notificación WebSocket
                if (existingCitaMedica.getEstado() == EstadoCita.ESPERANDO_MEDICO
                        && estadoAnterior != EstadoCita.ESPERANDO_MEDICO) {
                    String nombrePaciente = existingCitaMedica.getPaciente() != null
                            ? existingCitaMedica.getPaciente().getNombres() + " " + existingCitaMedica.getPaciente().getApellidos()
                            : "Paciente";
                    NotificacionDTO noti = new NotificacionDTO(
                            "PACIENTE_LISTO",
                            nombrePaciente + " está listo para atención médica",
                            existingCitaMedica.getId(),
                            nombrePaciente);
                    
                    // Notificar al tópico general
                    notificacionService.notificarPacienteListo(noti);

                    // Si hay un médico asignado, notificar también de forma personal
                    if (existingCitaMedica.getUser() != null && existingCitaMedica.getUser().getLogin() != null) {
                        noti.setMedicoLogin(existingCitaMedica.getUser().getLogin());
                        notificacionService.notificarMedicoEspecifico(existingCitaMedica.getUser().getLogin(), noti);
                    }
                }

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
}
