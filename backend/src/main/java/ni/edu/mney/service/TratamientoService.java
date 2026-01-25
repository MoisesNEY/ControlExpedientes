package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.Tratamiento;
import ni.edu.mney.repository.TratamientoRepository;
import ni.edu.mney.service.dto.TratamientoDTO;
import ni.edu.mney.service.mapper.TratamientoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.Tratamiento}.
 */
@Service
@Transactional
public class TratamientoService {

    private static final Logger LOG = LoggerFactory.getLogger(TratamientoService.class);

    private final TratamientoRepository tratamientoRepository;

    private final TratamientoMapper tratamientoMapper;

    public TratamientoService(TratamientoRepository tratamientoRepository, TratamientoMapper tratamientoMapper) {
        this.tratamientoRepository = tratamientoRepository;
        this.tratamientoMapper = tratamientoMapper;
    }

    /**
     * Save a tratamiento.
     *
     * @param tratamientoDTO the entity to save.
     * @return the persisted entity.
     */
    public TratamientoDTO save(TratamientoDTO tratamientoDTO) {
        LOG.debug("Request to save Tratamiento : {}", tratamientoDTO);
        Tratamiento tratamiento = tratamientoMapper.toEntity(tratamientoDTO);
        tratamiento = tratamientoRepository.save(tratamiento);
        return tratamientoMapper.toDto(tratamiento);
    }

    /**
     * Update a tratamiento.
     *
     * @param tratamientoDTO the entity to save.
     * @return the persisted entity.
     */
    public TratamientoDTO update(TratamientoDTO tratamientoDTO) {
        LOG.debug("Request to update Tratamiento : {}", tratamientoDTO);
        Tratamiento tratamiento = tratamientoMapper.toEntity(tratamientoDTO);
        tratamiento = tratamientoRepository.save(tratamiento);
        return tratamientoMapper.toDto(tratamiento);
    }

    /**
     * Partially update a tratamiento.
     *
     * @param tratamientoDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TratamientoDTO> partialUpdate(TratamientoDTO tratamientoDTO) {
        LOG.debug("Request to partially update Tratamiento : {}", tratamientoDTO);

        return tratamientoRepository
            .findById(tratamientoDTO.getId())
            .map(existingTratamiento -> {
                tratamientoMapper.partialUpdate(existingTratamiento, tratamientoDTO);

                return existingTratamiento;
            })
            .map(tratamientoRepository::save)
            .map(tratamientoMapper::toDto);
    }

    /**
     * Get one tratamiento by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TratamientoDTO> findOne(Long id) {
        LOG.debug("Request to get Tratamiento : {}", id);
        return tratamientoRepository.findById(id).map(tratamientoMapper::toDto);
    }

    /**
     * Delete the tratamiento by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Tratamiento : {}", id);
        tratamientoRepository.deleteById(id);
    }
}
