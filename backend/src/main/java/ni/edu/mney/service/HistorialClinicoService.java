package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.HistorialClinico;
import ni.edu.mney.repository.HistorialClinicoRepository;
import ni.edu.mney.service.dto.HistorialClinicoDTO;
import ni.edu.mney.service.mapper.HistorialClinicoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.HistorialClinico}.
 */
@Service
@Transactional
public class HistorialClinicoService {

    private static final Logger LOG = LoggerFactory.getLogger(HistorialClinicoService.class);

    private final HistorialClinicoRepository historialClinicoRepository;

    private final HistorialClinicoMapper historialClinicoMapper;

    public HistorialClinicoService(HistorialClinicoRepository historialClinicoRepository, HistorialClinicoMapper historialClinicoMapper) {
        this.historialClinicoRepository = historialClinicoRepository;
        this.historialClinicoMapper = historialClinicoMapper;
    }

    /**
     * Save a historialClinico.
     *
     * @param historialClinicoDTO the entity to save.
     * @return the persisted entity.
     */
    public HistorialClinicoDTO save(HistorialClinicoDTO historialClinicoDTO) {
        LOG.debug("Request to save HistorialClinico : {}", historialClinicoDTO);
        HistorialClinico historialClinico = historialClinicoMapper.toEntity(historialClinicoDTO);
        historialClinico = historialClinicoRepository.save(historialClinico);
        return historialClinicoMapper.toDto(historialClinico);
    }

    /**
     * Update a historialClinico.
     *
     * @param historialClinicoDTO the entity to save.
     * @return the persisted entity.
     */
    public HistorialClinicoDTO update(HistorialClinicoDTO historialClinicoDTO) {
        LOG.debug("Request to update HistorialClinico : {}", historialClinicoDTO);
        HistorialClinico historialClinico = historialClinicoMapper.toEntity(historialClinicoDTO);
        historialClinico = historialClinicoRepository.save(historialClinico);
        return historialClinicoMapper.toDto(historialClinico);
    }

    /**
     * Partially update a historialClinico.
     *
     * @param historialClinicoDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<HistorialClinicoDTO> partialUpdate(HistorialClinicoDTO historialClinicoDTO) {
        LOG.debug("Request to partially update HistorialClinico : {}", historialClinicoDTO);

        return historialClinicoRepository
            .findById(historialClinicoDTO.getId())
            .map(existingHistorialClinico -> {
                historialClinicoMapper.partialUpdate(existingHistorialClinico, historialClinicoDTO);

                return existingHistorialClinico;
            })
            .map(historialClinicoRepository::save)
            .map(historialClinicoMapper::toDto);
    }

    /**
     * Get one historialClinico by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<HistorialClinicoDTO> findOne(Long id) {
        LOG.debug("Request to get HistorialClinico : {}", id);
        return historialClinicoRepository.findById(id).map(historialClinicoMapper::toDto);
    }

    /**
     * Delete the historialClinico by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete HistorialClinico : {}", id);
        historialClinicoRepository.deleteById(id);
    }
}
