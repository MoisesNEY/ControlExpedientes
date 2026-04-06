package ni.edu.mney.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ni.edu.mney.domain.InteraccionMedicamentosa;
import ni.edu.mney.repository.InteraccionMedicamentosaRepository;
import ni.edu.mney.service.dto.InteraccionMedicamentosaDTO;
import ni.edu.mney.service.mapper.InteraccionMedicamentosaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.InteraccionMedicamentosa}.
 */
@Service
@Transactional
public class InteraccionMedicamentosaService {

    private static final Logger LOG = LoggerFactory.getLogger(InteraccionMedicamentosaService.class);

    private final InteraccionMedicamentosaRepository interaccionMedicamentosaRepository;

    private final InteraccionMedicamentosaMapper interaccionMedicamentosaMapper;

    public InteraccionMedicamentosaService(
        InteraccionMedicamentosaRepository interaccionMedicamentosaRepository,
        InteraccionMedicamentosaMapper interaccionMedicamentosaMapper
    ) {
        this.interaccionMedicamentosaRepository = interaccionMedicamentosaRepository;
        this.interaccionMedicamentosaMapper = interaccionMedicamentosaMapper;
    }

    /**
     * Save an interaccionMedicamentosa.
     *
     * @param interaccionMedicamentosaDTO the entity to save.
     * @return the persisted entity.
     */
    public InteraccionMedicamentosaDTO save(InteraccionMedicamentosaDTO interaccionMedicamentosaDTO) {
        LOG.debug("Request to save InteraccionMedicamentosa : {}", interaccionMedicamentosaDTO);
        InteraccionMedicamentosa interaccionMedicamentosa = interaccionMedicamentosaMapper.toEntity(interaccionMedicamentosaDTO);
        interaccionMedicamentosa = interaccionMedicamentosaRepository.save(interaccionMedicamentosa);
        return interaccionMedicamentosaMapper.toDto(interaccionMedicamentosa);
    }

    /**
     * Update an interaccionMedicamentosa.
     *
     * @param interaccionMedicamentosaDTO the entity to save.
     * @return the persisted entity.
     */
    public InteraccionMedicamentosaDTO update(InteraccionMedicamentosaDTO interaccionMedicamentosaDTO) {
        LOG.debug("Request to update InteraccionMedicamentosa : {}", interaccionMedicamentosaDTO);
        InteraccionMedicamentosa interaccionMedicamentosa = interaccionMedicamentosaMapper.toEntity(interaccionMedicamentosaDTO);
        interaccionMedicamentosa = interaccionMedicamentosaRepository.save(interaccionMedicamentosa);
        return interaccionMedicamentosaMapper.toDto(interaccionMedicamentosa);
    }

    /**
     * Get all the interaccionMedicamentosas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<InteraccionMedicamentosaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all InteraccionMedicamentosas");
        return interaccionMedicamentosaRepository.findAll(pageable).map(interaccionMedicamentosaMapper::toDto);
    }

    /**
     * Get one interaccionMedicamentosa by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<InteraccionMedicamentosaDTO> findOne(Long id) {
        LOG.debug("Request to get InteraccionMedicamentosa : {}", id);
        return interaccionMedicamentosaRepository.findById(id).map(interaccionMedicamentosaMapper::toDto);
    }

    /**
     * Delete the interaccionMedicamentosa by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete InteraccionMedicamentosa : {}", id);
        interaccionMedicamentosaRepository.deleteById(id);
    }

    /**
     * Verify drug interactions among a list of medication IDs.
     * Returns all known interactions where both medications are in the provided list.
     *
     * @param medicamentoIds the list of medication IDs being prescribed.
     * @return the list of interactions found among the given medications.
     */
    @Transactional(readOnly = true)
    public List<InteraccionMedicamentosaDTO> verificarInteracciones(List<Long> medicamentoIds) {
        LOG.debug("Request to verify drug interactions for medicamento IDs : {}", medicamentoIds);
        if (medicamentoIds == null || medicamentoIds.size() < 2) {
            return List.of();
        }
        return interaccionMedicamentosaRepository.findInteractionsBetweenMedicamentos(medicamentoIds)
            .stream()
            .map(interaccionMedicamentosaMapper::toDto)
            .collect(Collectors.toList());
    }
}
