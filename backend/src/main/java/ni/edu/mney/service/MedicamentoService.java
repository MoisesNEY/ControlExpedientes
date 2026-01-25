package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.repository.MedicamentoRepository;
import ni.edu.mney.service.dto.MedicamentoDTO;
import ni.edu.mney.service.mapper.MedicamentoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * Delete the medicamento by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Medicamento : {}", id);
        medicamentoRepository.deleteById(id);
    }
}
