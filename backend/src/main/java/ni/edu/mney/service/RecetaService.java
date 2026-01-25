package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.Receta;
import ni.edu.mney.repository.RecetaRepository;
import ni.edu.mney.service.dto.RecetaDTO;
import ni.edu.mney.service.mapper.RecetaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.Receta}.
 */
@Service
@Transactional
public class RecetaService {

    private static final Logger LOG = LoggerFactory.getLogger(RecetaService.class);

    private final RecetaRepository recetaRepository;

    private final RecetaMapper recetaMapper;

    public RecetaService(RecetaRepository recetaRepository, RecetaMapper recetaMapper) {
        this.recetaRepository = recetaRepository;
        this.recetaMapper = recetaMapper;
    }

    /**
     * Save a receta.
     *
     * @param recetaDTO the entity to save.
     * @return the persisted entity.
     */
    public RecetaDTO save(RecetaDTO recetaDTO) {
        LOG.debug("Request to save Receta : {}", recetaDTO);
        Receta receta = recetaMapper.toEntity(recetaDTO);
        receta = recetaRepository.save(receta);
        return recetaMapper.toDto(receta);
    }

    /**
     * Update a receta.
     *
     * @param recetaDTO the entity to save.
     * @return the persisted entity.
     */
    public RecetaDTO update(RecetaDTO recetaDTO) {
        LOG.debug("Request to update Receta : {}", recetaDTO);
        Receta receta = recetaMapper.toEntity(recetaDTO);
        receta = recetaRepository.save(receta);
        return recetaMapper.toDto(receta);
    }

    /**
     * Partially update a receta.
     *
     * @param recetaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RecetaDTO> partialUpdate(RecetaDTO recetaDTO) {
        LOG.debug("Request to partially update Receta : {}", recetaDTO);

        return recetaRepository
            .findById(recetaDTO.getId())
            .map(existingReceta -> {
                recetaMapper.partialUpdate(existingReceta, recetaDTO);

                return existingReceta;
            })
            .map(recetaRepository::save)
            .map(recetaMapper::toDto);
    }

    /**
     * Get one receta by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RecetaDTO> findOne(Long id) {
        LOG.debug("Request to get Receta : {}", id);
        return recetaRepository.findById(id).map(recetaMapper::toDto);
    }

    /**
     * Delete the receta by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Receta : {}", id);
        recetaRepository.deleteById(id);
    }
}
