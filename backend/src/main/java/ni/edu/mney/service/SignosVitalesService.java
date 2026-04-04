package ni.edu.mney.service;

import java.util.Optional;
import ni.edu.mney.domain.SignosVitales;
import ni.edu.mney.repository.SignosVitalesRepository;
import ni.edu.mney.service.dto.SignosVitalesDTO;
import ni.edu.mney.service.mapper.SignosVitalesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.SignosVitales}.
 */
@Service
@Transactional
public class SignosVitalesService {

    private static final Logger LOG = LoggerFactory.getLogger(SignosVitalesService.class);

    private final SignosVitalesRepository signosVitalesRepository;

    private final SignosVitalesMapper signosVitalesMapper;

    public SignosVitalesService(SignosVitalesRepository signosVitalesRepository,
            SignosVitalesMapper signosVitalesMapper) {
        this.signosVitalesRepository = signosVitalesRepository;
        this.signosVitalesMapper = signosVitalesMapper;
    }

    /**
     * Save a signosVitales.
     *
     * @param signosVitalesDTO the entity to save.
     * @return the persisted entity.
     */
    public SignosVitalesDTO save(SignosVitalesDTO signosVitalesDTO) {
        LOG.debug("Request to save SignosVitales : {}", signosVitalesDTO);
        SignosVitales signosVitales = signosVitalesMapper.toEntity(signosVitalesDTO);
        signosVitales = signosVitalesRepository.save(signosVitales);
        return signosVitalesMapper.toDto(signosVitales);
    }

    /**
     * Update a signosVitales.
     *
     * @param signosVitalesDTO the entity to save.
     * @return the persisted entity.
     */
    public SignosVitalesDTO update(SignosVitalesDTO signosVitalesDTO) {
        LOG.debug("Request to update SignosVitales : {}", signosVitalesDTO);
        SignosVitales signosVitales = signosVitalesMapper.toEntity(signosVitalesDTO);
        signosVitales = signosVitalesRepository.save(signosVitales);
        return signosVitalesMapper.toDto(signosVitales);
    }

    /**
     * Partially update a signosVitales.
     *
     * @param signosVitalesDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SignosVitalesDTO> partialUpdate(SignosVitalesDTO signosVitalesDTO) {
        LOG.debug("Request to partially update SignosVitales : {}", signosVitalesDTO);

        return signosVitalesRepository
                .findById(signosVitalesDTO.getId())
                .map(existingSignosVitales -> {
                    signosVitalesMapper.partialUpdate(existingSignosVitales, signosVitalesDTO);

                    return existingSignosVitales;
                })
                .map(signosVitalesRepository::save)
                .map(signosVitalesMapper::toDto);
    }

    /**
     * Get one signosVitales by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SignosVitalesDTO> findOne(Long id) {
        LOG.debug("Request to get SignosVitales : {}", id);
        return signosVitalesRepository.findById(id).map(signosVitalesMapper::toDto);
    }

    /**
     * Delete the signosVitales by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete SignosVitales : {}", id);
        signosVitalesRepository.deleteById(id);
    }

    /**
     * Get the latest SignosVitales by pacienteId and current date.
     *
     * @param pacienteId the id of the paciente.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public java.util.List<SignosVitalesDTO> findLatestByPacienteIdAndDate(Long pacienteId, java.time.LocalDate date) {
        LOG.debug("Request to get SignosVitales for Paciente : {} and Date : {}", pacienteId, date);
        return signosVitalesRepository.findByPacienteIdAndFechaConsulta(pacienteId, date).stream()
                .map(signosVitalesMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
