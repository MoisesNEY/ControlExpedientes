package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.mapper.ConsultaMedicaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link ni.edu.mney.domain.ConsultaMedica}.
 */
@Service
@Transactional
public class ConsultaMedicaService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultaMedicaService.class);

    private final ConsultaMedicaRepository consultaMedicaRepository;

    private final ConsultaMedicaMapper consultaMedicaMapper;

    public ConsultaMedicaService(ConsultaMedicaRepository consultaMedicaRepository,
            ConsultaMedicaMapper consultaMedicaMapper) {
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.consultaMedicaMapper = consultaMedicaMapper;
    }

    /**
     * Save a consultaMedica.
     *
     * @param consultaMedicaDTO the entity to save.
     * @return the persisted entity.
     */
    public ConsultaMedicaDTO save(ConsultaMedicaDTO consultaMedicaDTO) {
        LOG.debug("Request to save ConsultaMedica : {}", consultaMedicaDTO);
        ConsultaMedica consultaMedica = consultaMedicaMapper.toEntity(consultaMedicaDTO);
        consultaMedica = consultaMedicaRepository.save(consultaMedica);
        return consultaMedicaMapper.toDto(consultaMedica);
    }

    /**
     * Update a consultaMedica.
     *
     * @param consultaMedicaDTO the entity to save.
     * @return the persisted entity.
     */
    public ConsultaMedicaDTO update(ConsultaMedicaDTO consultaMedicaDTO) {
        LOG.debug("Request to update ConsultaMedica : {}", consultaMedicaDTO);
        ConsultaMedica consultaMedica = consultaMedicaMapper.toEntity(consultaMedicaDTO);
        consultaMedica = consultaMedicaRepository.save(consultaMedica);
        return consultaMedicaMapper.toDto(consultaMedica);
    }

    /**
     * Partially update a consultaMedica.
     *
     * @param consultaMedicaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ConsultaMedicaDTO> partialUpdate(ConsultaMedicaDTO consultaMedicaDTO) {
        LOG.debug("Request to partially update ConsultaMedica : {}", consultaMedicaDTO);

        return consultaMedicaRepository
                .findById(consultaMedicaDTO.getId())
                .map(existingConsultaMedica -> {
                    consultaMedicaMapper.partialUpdate(existingConsultaMedica, consultaMedicaDTO);

                    return existingConsultaMedica;
                })
                .map(consultaMedicaRepository::save)
                .map(consultaMedicaMapper::toDto);
    }

    /**
     * Get all the consultaMedicas with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ConsultaMedicaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return consultaMedicaRepository.findAllWithEagerRelationships(pageable).map(consultaMedicaMapper::toDto);
    }

    /**
     * Get one consultaMedica by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ConsultaMedicaDTO> findOne(Long id) {
        LOG.debug("Request to get ConsultaMedica : {}", id);
        return consultaMedicaRepository.findOneWithDetailsById(id).map(consultaMedicaMapper::toDto);
    }

    /**
     * Delete the consultaMedica by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ConsultaMedica : {}", id);
        consultaMedicaRepository.deleteById(id);
    }
}
