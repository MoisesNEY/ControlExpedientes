package ni.edu.mney.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ni.edu.mney.domain.ResultadoLaboratorio;
import ni.edu.mney.repository.ResultadoLaboratorioRepository;
import ni.edu.mney.service.dto.ResultadoLaboratorioDTO;
import ni.edu.mney.service.mapper.ResultadoLaboratorioMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ni.edu.mney.domain.ResultadoLaboratorio}.
 */
@Service
@Transactional
public class ResultadoLaboratorioService {

    private static final Logger LOG = LoggerFactory.getLogger(ResultadoLaboratorioService.class);

    private final ResultadoLaboratorioRepository resultadoLaboratorioRepository;

    private final ResultadoLaboratorioMapper resultadoLaboratorioMapper;

    public ResultadoLaboratorioService(
            ResultadoLaboratorioRepository resultadoLaboratorioRepository,
            ResultadoLaboratorioMapper resultadoLaboratorioMapper) {
        this.resultadoLaboratorioRepository = resultadoLaboratorioRepository;
        this.resultadoLaboratorioMapper = resultadoLaboratorioMapper;
    }

    /**
     * Save a resultadoLaboratorio.
     *
     * @param resultadoLaboratorioDTO the entity to save.
     * @return the persisted entity.
     */
    public ResultadoLaboratorioDTO save(ResultadoLaboratorioDTO resultadoLaboratorioDTO) {
        LOG.debug("Request to save ResultadoLaboratorio : {}", resultadoLaboratorioDTO);
        ResultadoLaboratorio resultadoLaboratorio = resultadoLaboratorioMapper.toEntity(resultadoLaboratorioDTO);
        resultadoLaboratorio = resultadoLaboratorioRepository.save(resultadoLaboratorio);
        return resultadoLaboratorioMapper.toDto(resultadoLaboratorio);
    }

    /**
     * Update a resultadoLaboratorio.
     *
     * @param resultadoLaboratorioDTO the entity to save.
     * @return the persisted entity.
     */
    public ResultadoLaboratorioDTO update(ResultadoLaboratorioDTO resultadoLaboratorioDTO) {
        LOG.debug("Request to update ResultadoLaboratorio : {}", resultadoLaboratorioDTO);
        ResultadoLaboratorio resultadoLaboratorio = resultadoLaboratorioMapper.toEntity(resultadoLaboratorioDTO);
        resultadoLaboratorio = resultadoLaboratorioRepository.save(resultadoLaboratorio);
        return resultadoLaboratorioMapper.toDto(resultadoLaboratorio);
    }

    /**
     * Partially update a resultadoLaboratorio.
     *
     * @param resultadoLaboratorioDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ResultadoLaboratorioDTO> partialUpdate(ResultadoLaboratorioDTO resultadoLaboratorioDTO) {
        LOG.debug("Request to partially update ResultadoLaboratorio : {}", resultadoLaboratorioDTO);

        return resultadoLaboratorioRepository
                .findById(resultadoLaboratorioDTO.getId())
                .map(existingResultadoLaboratorio -> {
                    resultadoLaboratorioMapper.partialUpdate(existingResultadoLaboratorio, resultadoLaboratorioDTO);

                    return existingResultadoLaboratorio;
                })
                .map(resultadoLaboratorioRepository::save)
                .map(resultadoLaboratorioMapper::toDto);
    }

    /**
     * Get all the resultadoLaboratorios.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResultadoLaboratorioDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ResultadoLaboratorios");
        return resultadoLaboratorioRepository.findAll(pageable).map(resultadoLaboratorioMapper::toDto);
    }

    /**
     * Get one resultadoLaboratorio by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ResultadoLaboratorioDTO> findOne(Long id) {
        LOG.debug("Request to get ResultadoLaboratorio : {}", id);
        return resultadoLaboratorioRepository.findById(id).map(resultadoLaboratorioMapper::toDto);
    }

    /**
     * Get all resultadoLaboratorios by paciente id.
     *
     * @param pacienteId the id of the paciente.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResultadoLaboratorioDTO> findByPacienteId(Long pacienteId, Pageable pageable) {
        LOG.debug("Request to get ResultadoLaboratorios by Paciente : {}", pacienteId);
        return resultadoLaboratorioRepository.findByPacienteId(pacienteId, pageable)
                .map(resultadoLaboratorioMapper::toDto);
    }

    /**
     * Get all resultadoLaboratorios by consulta id.
     *
     * @param consultaId the id of the consulta.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ResultadoLaboratorioDTO> findByConsultaId(Long consultaId) {
        LOG.debug("Request to get ResultadoLaboratorios by Consulta : {}", consultaId);
        return resultadoLaboratorioRepository.findByConsultaId(consultaId).stream()
                .map(resultadoLaboratorioMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete the resultadoLaboratorio by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ResultadoLaboratorio : {}", id);
        resultadoLaboratorioRepository.deleteById(id);
    }
}
