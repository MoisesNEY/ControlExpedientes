package ni.edu.mney.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.mapper.ExpedienteClinicoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.ExpedienteClinico}.
 */
@Service
@Transactional
public class ExpedienteClinicoService {

    private static final Logger LOG = LoggerFactory.getLogger(ExpedienteClinicoService.class);

    private final ExpedienteClinicoRepository expedienteClinicoRepository;

    private final ExpedienteClinicoMapper expedienteClinicoMapper;

    public ExpedienteClinicoService(
        ExpedienteClinicoRepository expedienteClinicoRepository,
        ExpedienteClinicoMapper expedienteClinicoMapper
    ) {
        this.expedienteClinicoRepository = expedienteClinicoRepository;
        this.expedienteClinicoMapper = expedienteClinicoMapper;
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
     *  Get all the expedienteClinicos where Paciente is {@code null}.
     *  @return the list of entities.
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
     * Delete the expedienteClinico by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ExpedienteClinico : {}", id);
        expedienteClinicoRepository.deleteById(id);
    }
}
