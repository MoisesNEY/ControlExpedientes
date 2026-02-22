package ni.edu.mney.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.repository.MedicamentoRepository;
import ni.edu.mney.service.dto.MedicamentoDTO;
import ni.edu.mney.service.mapper.MedicamentoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.Medicamento}.
 */
@Service
@Transactional
public class MedicamentoService {

    private static final Logger LOG = LoggerFactory.getLogger(MedicamentoService.class);

    private final MedicamentoRepository medicamentoRepository;

    private final MedicamentoMapper medicamentoMapper;

    public MedicamentoService(MedicamentoRepository medicamentoRepository, MedicamentoMapper medicamentoMapper) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicamentoMapper = medicamentoMapper;
    }

    /**
     * Save a medicamento.
     *
     * @param medicamentoDTO the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.MedicamentoService.lowStock", allEntries = true)
    public MedicamentoDTO save(MedicamentoDTO medicamentoDTO) {
        LOG.debug("Request to save Medicamento : {}", medicamentoDTO);
        Medicamento medicamento = medicamentoMapper.toEntity(medicamentoDTO);
        medicamento = medicamentoRepository.save(medicamento);
        return medicamentoMapper.toDto(medicamento);
    }

    /**
     * Update a medicamento.
     *
     * @param medicamentoDTO the entity to save.
     * @return the persisted entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.MedicamentoService.lowStock", allEntries = true)
    public MedicamentoDTO update(MedicamentoDTO medicamentoDTO) {
        LOG.debug("Request to update Medicamento : {}", medicamentoDTO);
        Medicamento medicamento = medicamentoMapper.toEntity(medicamentoDTO);
        medicamento = medicamentoRepository.save(medicamento);
        return medicamentoMapper.toDto(medicamento);
    }

    /**
     * Partially update a medicamento.
     *
     * @param medicamentoDTO the entity to update partially.
     * @return the persisted entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.MedicamentoService.lowStock", allEntries = true)
    public Optional<MedicamentoDTO> partialUpdate(MedicamentoDTO medicamentoDTO) {
        LOG.debug("Request to partially update Medicamento : {}", medicamentoDTO);

        return medicamentoRepository
                .findById(medicamentoDTO.getId())
                .map(existingMedicamento -> {
                    medicamentoMapper.partialUpdate(existingMedicamento, medicamentoDTO);

                    return existingMedicamento;
                })
                .map(medicamentoRepository::save)
                .map(medicamentoMapper::toDto);
    }

    /**
     * Get one medicamento by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MedicamentoDTO> findOne(Long id) {
        LOG.debug("Request to get Medicamento : {}", id);
        return medicamentoRepository.findById(id).map(medicamentoMapper::toDto);
    }

    /**
     * Get all medicamentos with stock less than threshold.
     *
     * @param threshold the stock threshold.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "ni.edu.mney.service.MedicamentoService.lowStock")
    public List<MedicamentoDTO> findAllLowStock(Integer threshold) {
        LOG.debug("Request to get all Medicamentos with stock less than : {}", threshold);
        return medicamentoRepository.findByStockLessThan(threshold).stream().map(medicamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete the medicamento by id.
     *
     * @param id the id of the entity.
     */
    @CacheEvict(value = "ni.edu.mney.service.MedicamentoService.lowStock", allEntries = true)
    public void delete(Long id) {
        LOG.debug("Request to delete Medicamento : {}", id);
        medicamentoRepository.deleteById(id);
    }
}
