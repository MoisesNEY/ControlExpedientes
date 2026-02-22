package ni.edu.mney.service;

import java.time.LocalDate;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.dto.TimelineEntryDTO;
import ni.edu.mney.service.mapper.ExpedienteClinicoMapper;
import ni.edu.mney.service.mapper.SignosVitalesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link ni.edu.mney.domain.ExpedienteClinico}.
 */
@Service
@Transactional
public class ExpedienteClinicoService {

    private static final Logger LOG = LoggerFactory.getLogger(ExpedienteClinicoService.class);

    private final ExpedienteClinicoRepository expedienteClinicoRepository;

    private final ExpedienteClinicoMapper expedienteClinicoMapper;

    private final SignosVitalesMapper signosVitalesMapper;

    public ExpedienteClinicoService(
            ExpedienteClinicoRepository expedienteClinicoRepository,
            ExpedienteClinicoMapper expedienteClinicoMapper,
            SignosVitalesMapper signosVitalesMapper) {
        this.expedienteClinicoRepository = expedienteClinicoRepository;
        this.expedienteClinicoMapper = expedienteClinicoMapper;
        this.signosVitalesMapper = signosVitalesMapper;
    }

    /**
     * Generates the next clinical record number in the format EXP-YYYY-XXXX.
     * 
     * @return a unique sequential string.
     */
    public String generateNextNumeroExpediente() {
        int year = LocalDate.now().getYear();
        String prefix = "EXP-" + year + "-";

        return expedienteClinicoRepository.findTopByOrderByIdDesc()
                .map(last -> {
                    String lastNum = last.getNumeroExpediente();
                    if (lastNum != null && lastNum.startsWith(prefix)) {
                        try {
                            int sequence = Integer.parseInt(lastNum.substring(prefix.length())) + 1;
                            return String.format("%s%04d", prefix, sequence);
                        } catch (NumberFormatException e) {
                            return prefix + "0001";
                        }
                    }
                    return prefix + "0001";
                })
                .orElse(prefix + "0001");
    }

    /**
     * Save a expedienteClinico.
     *
     * @param expedienteClinicoDTO the entity to save.
     * @return the persisted entity.
     */
    public ExpedienteClinicoDTO save(ExpedienteClinicoDTO expedienteClinicoDTO) {
        LOG.debug("Request to save ExpedienteClinico : {}", expedienteClinicoDTO);
        ExpedienteClinico expedienteClinico = expedienteClinicoMapper.toEntity(expedienteClinicoDTO);
        expedienteClinico = expedienteClinicoRepository.save(expedienteClinico);
        return expedienteClinicoMapper.toDto(expedienteClinico);
    }

    /**
     * Update a expedienteClinico.
     *
     * @param expedienteClinicoDTO the entity to save.
     * @return the persisted entity.
     */
    public ExpedienteClinicoDTO update(ExpedienteClinicoDTO expedienteClinicoDTO) {
        LOG.debug("Request to update ExpedienteClinico : {}", expedienteClinicoDTO);
        ExpedienteClinico expedienteClinico = expedienteClinicoMapper.toEntity(expedienteClinicoDTO);
        expedienteClinico = expedienteClinicoRepository.save(expedienteClinico);
        return expedienteClinicoMapper.toDto(expedienteClinico);
    }

    /**
     * Partially update a expedienteClinico.
     *
     * @param expedienteClinicoDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ExpedienteClinicoDTO> partialUpdate(ExpedienteClinicoDTO expedienteClinicoDTO) {
        LOG.debug("Request to partially update ExpedienteClinico : {}", expedienteClinicoDTO);

        return expedienteClinicoRepository
                .findById(expedienteClinicoDTO.getId())
                .map(existingExpedienteClinico -> {
                    expedienteClinicoMapper.partialUpdate(existingExpedienteClinico, expedienteClinicoDTO);

                    return existingExpedienteClinico;
                })
                .map(expedienteClinicoRepository::save)
                .map(expedienteClinicoMapper::toDto);
    }

    /**
     * Get all the expedienteClinicos where Paciente is {@code null}.
     * 
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ExpedienteClinicoDTO> findAllWherePacienteIsNull() {
        LOG.debug("Request to get all expedienteClinicos where Paciente is null");
        return StreamSupport.stream(expedienteClinicoRepository.findAll().spliterator(), false)
                .filter(expedienteClinico -> expedienteClinico.getPaciente() == null)
                .map(expedienteClinicoMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one expedienteClinico by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ExpedienteClinicoDTO> findOne(Long id) {
        LOG.debug("Request to get ExpedienteClinico : {}", id);
        return expedienteClinicoRepository.findById(id).map(expedienteClinicoMapper::toDto);
    }

    /**
     * Get one expedienteClinico by paciente id.
     *
     * @param pacienteId the id of the paciente.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ExpedienteClinicoDTO> findByPacienteId(Long pacienteId) {
        LOG.debug("Request to get ExpedienteClinico by paciente id : {}", pacienteId);
        return expedienteClinicoRepository.findByPacienteId(pacienteId).map(expedienteClinicoMapper::toDto);
    }

    /**
     * Delete the expedienteClinico by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ExpedienteClinico : {}", id);
        expedienteClinicoRepository.deleteById(id);
    }

    /**
     * Get the chronological clinical timeline for an expediente.
     * 
     * @param id the id of the expediente.
     * @return a list of timeline entries.
     */
    @Transactional(readOnly = true)
    public List<TimelineEntryDTO> getTimeline(Long id) {
        LOG.debug("Request to get clinical timeline for expediente : {}", id);
        return expedienteClinicoRepository.findOneWithTimelineData(id)
                .map(expediente -> expediente.getConsultas().stream()
                        .sorted((c1, c2) -> c2.getFechaConsulta().compareTo(c1.getFechaConsulta()))
                        .map(consulta -> {
                            TimelineEntryDTO entry = new TimelineEntryDTO();
                            entry.setFecha(consulta.getFechaConsulta());
                            entry.setMotivo(consulta.getMotivoConsulta());

                            // Doctor Info
                            if (consulta.getUser() != null) {
                                String name = (consulta.getUser().getFirstName() + " "
                                        + consulta.getUser().getLastName()).trim();
                                entry.setProfesional(name.isEmpty() ? consulta.getUser().getLogin() : name);
                            }

                            // Signos Vitales
                            if (!consulta.getSignosVitales().isEmpty()) {
                                entry.setSignosVitales(
                                        signosVitalesMapper.toDto(consulta.getSignosVitales().iterator().next()));
                            }

                            // Diagnósticos
                            entry.setDiagnosticos(consulta.getDiagnosticos().stream()
                                    .map(d -> (d.getCodigoCIE() != null ? d.getCodigoCIE() + " - " : "")
                                            + d.getDescripcion())
                                    .collect(Collectors.toList()));

                            // Recetas
                            entry.setRecetas(consulta.getRecetas().stream()
                                    .map(r -> {
                                        String med = r.getMedicamento() != null ? r.getMedicamento().getNombre()
                                                : "Medicamento desconocido";
                                        return String.format("%s (%d) - %s, %s por %s",
                                                med, r.getCantidad(), r.getDosis(), r.getFrecuencia(), r.getDuracion());
                                    })
                                    .collect(Collectors.toList()));

                            return entry;
                        }).collect(Collectors.toList()))
                .orElse(new LinkedList<>());
    }
}
