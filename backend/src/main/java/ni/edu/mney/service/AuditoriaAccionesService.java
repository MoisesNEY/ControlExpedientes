package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.AuditoriaAcciones;
import ni.edu.mney.repository.AuditoriaAccionesRepository;
import ni.edu.mney.service.dto.AuditoriaAccionesDTO;
import ni.edu.mney.service.mapper.AuditoriaAccionesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.AuditoriaAcciones}.
 */
@Service
@Transactional
public class AuditoriaAccionesService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaAccionesService.class);

    private final AuditoriaAccionesRepository auditoriaAccionesRepository;

    private final AuditoriaAccionesMapper auditoriaAccionesMapper;

    public AuditoriaAccionesService(
        AuditoriaAccionesRepository auditoriaAccionesRepository,
        AuditoriaAccionesMapper auditoriaAccionesMapper
    ) {
        this.auditoriaAccionesRepository = auditoriaAccionesRepository;
        this.auditoriaAccionesMapper = auditoriaAccionesMapper;
    }

    /**
     * Save a auditoriaAcciones.
     *
     * @param auditoriaAccionesDTO the entity to save.
     * @return the persisted entity.
     */
    public AuditoriaAccionesDTO save(AuditoriaAccionesDTO auditoriaAccionesDTO) {
        LOG.debug("Request to save AuditoriaAcciones : {}", auditoriaAccionesDTO);
        AuditoriaAcciones auditoriaAcciones = auditoriaAccionesMapper.toEntity(auditoriaAccionesDTO);
        auditoriaAcciones = auditoriaAccionesRepository.save(auditoriaAcciones);
        return auditoriaAccionesMapper.toDto(auditoriaAcciones);
    }

    /**
     * Update a auditoriaAcciones.
     *
     * @param auditoriaAccionesDTO the entity to save.
     * @return the persisted entity.
     */
    public AuditoriaAccionesDTO update(AuditoriaAccionesDTO auditoriaAccionesDTO) {
        LOG.debug("Request to update AuditoriaAcciones : {}", auditoriaAccionesDTO);
        AuditoriaAcciones auditoriaAcciones = auditoriaAccionesMapper.toEntity(auditoriaAccionesDTO);
        auditoriaAcciones = auditoriaAccionesRepository.save(auditoriaAcciones);
        return auditoriaAccionesMapper.toDto(auditoriaAcciones);
    }

    /**
     * Partially update a auditoriaAcciones.
     *
     * @param auditoriaAccionesDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AuditoriaAccionesDTO> partialUpdate(AuditoriaAccionesDTO auditoriaAccionesDTO) {
        LOG.debug("Request to partially update AuditoriaAcciones : {}", auditoriaAccionesDTO);

        return auditoriaAccionesRepository
            .findById(auditoriaAccionesDTO.getId())
            .map(existingAuditoriaAcciones -> {
                auditoriaAccionesMapper.partialUpdate(existingAuditoriaAcciones, auditoriaAccionesDTO);

                return existingAuditoriaAcciones;
            })
            .map(auditoriaAccionesRepository::save)
            .map(auditoriaAccionesMapper::toDto);
    }

    /**
     * Get all the auditoriaAcciones with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AuditoriaAccionesDTO> findAllWithEagerRelationships(Pageable pageable) {
        return auditoriaAccionesRepository.findAllWithEagerRelationships(pageable).map(auditoriaAccionesMapper::toDto);
    }

    /**
     * Get one auditoriaAcciones by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AuditoriaAccionesDTO> findOne(Long id) {
        LOG.debug("Request to get AuditoriaAcciones : {}", id);
        return auditoriaAccionesRepository.findOneWithEagerRelationships(id).map(auditoriaAccionesMapper::toDto);
    }

    /**
     * Delete the auditoriaAcciones by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete AuditoriaAcciones : {}", id);
        auditoriaAccionesRepository.deleteById(id);
    }
}
