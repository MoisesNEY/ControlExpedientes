package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.Diagnostico;
import ni.edu.mney.repository.DiagnosticoRepository;
import ni.edu.mney.service.dto.DiagnosticoDTO;
import ni.edu.mney.service.mapper.DiagnosticoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.Diagnostico}.
 */
@Service
@Transactional
public class DiagnosticoService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticoService.class);

    private final DiagnosticoRepository diagnosticoRepository;

    private final DiagnosticoMapper diagnosticoMapper;

    public DiagnosticoService(DiagnosticoRepository diagnosticoRepository, DiagnosticoMapper diagnosticoMapper) {
        this.diagnosticoRepository = diagnosticoRepository;
        this.diagnosticoMapper = diagnosticoMapper;
    }

    /**
     * Save a diagnostico.
     *
     * @param diagnosticoDTO the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.DiagnosticoService.search", allEntries = true)
    public DiagnosticoDTO save(DiagnosticoDTO diagnosticoDTO) {
        LOG.debug("Request to save Diagnostico : {}", diagnosticoDTO);
        Diagnostico diagnostico = diagnosticoMapper.toEntity(diagnosticoDTO);
        diagnostico = diagnosticoRepository.save(diagnostico);
        return diagnosticoMapper.toDto(diagnostico);
    }

    /**
     * Update a diagnostico.
     *
     * @param diagnosticoDTO the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.DiagnosticoService.search", allEntries = true)
    public DiagnosticoDTO update(DiagnosticoDTO diagnosticoDTO) {
        LOG.debug("Request to update Diagnostico : {}", diagnosticoDTO);
        Diagnostico diagnostico = diagnosticoMapper.toEntity(diagnosticoDTO);
        diagnostico = diagnosticoRepository.save(diagnostico);
        return diagnosticoMapper.toDto(diagnostico);
    }

    /**
     * Partially update a diagnostico.
     *
     * @param diagnosticoDTO the entity to update partially.
     * @return the persisted entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.DiagnosticoService.search", allEntries = true)
    public Optional<DiagnosticoDTO> partialUpdate(DiagnosticoDTO diagnosticoDTO) {
        LOG.debug("Request to partially update Diagnostico : {}", diagnosticoDTO);

        return diagnosticoRepository
                .findById(diagnosticoDTO.getId())
                .map(existingDiagnostico -> {
                    diagnosticoMapper.partialUpdate(existingDiagnostico, diagnosticoDTO);

                    return existingDiagnostico;
                })
                .map(diagnosticoRepository::save)
                .map(diagnosticoMapper::toDto);
    }

    /**
     * Get one diagnostico by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DiagnosticoDTO> findOne(Long id) {
        LOG.debug("Request to get Diagnostico : {}", id);
        return diagnosticoRepository.findById(id).map(diagnosticoMapper::toDto);
    }

    /**
     * Delete the diagnostico by id.
     *
     * @param id the id of the entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.DiagnosticoService.search", allEntries = true)
    public void delete(Long id) {
        LOG.debug("Request to delete Diagnostico : {}", id);
        diagnosticoRepository.deleteById(id);
    }

    /**
     * Search for the diagnostico matching the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "ni.edu.mney.service.DiagnosticoService.search")
    public Page<DiagnosticoDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search Diagnosticos for query {}", query);
        return diagnosticoRepository.search(query, pageable).map(diagnosticoMapper::toDto);
    }
}
