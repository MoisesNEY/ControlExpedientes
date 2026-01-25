package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.service.dto.CitaMedicaDTO;
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

    private final CitaMedicaMapper citaMedicaMapper;

    public CitaMedicaService(CitaMedicaRepository citaMedicaRepository, CitaMedicaMapper citaMedicaMapper) {
        this.citaMedicaRepository = citaMedicaRepository;
        this.citaMedicaMapper = citaMedicaMapper;
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
                citaMedicaMapper.partialUpdate(existingCitaMedica, citaMedicaDTO);

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
}
