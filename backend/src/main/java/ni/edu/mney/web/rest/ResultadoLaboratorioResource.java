package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.ResultadoLaboratorioRepository;
import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.ResultadoLaboratorioService;
import ni.edu.mney.service.dto.ResultadoLaboratorioDTO;
import ni.edu.mney.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ni.edu.mney.domain.ResultadoLaboratorio}.
 */
@RestController
@RequestMapping("/api/resultados-laboratorio")
public class ResultadoLaboratorioResource {

    private static final Logger LOG = LoggerFactory.getLogger(ResultadoLaboratorioResource.class);

    private static final String ENTITY_NAME = "resultadoLaboratorio";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ResultadoLaboratorioService resultadoLaboratorioService;

    private final ResultadoLaboratorioRepository resultadoLaboratorioRepository;

    public ResultadoLaboratorioResource(
            ResultadoLaboratorioService resultadoLaboratorioService,
            ResultadoLaboratorioRepository resultadoLaboratorioRepository) {
        this.resultadoLaboratorioService = resultadoLaboratorioService;
        this.resultadoLaboratorioRepository = resultadoLaboratorioRepository;
    }

    /**
     * {@code POST  /resultados-laboratorio} : Create a new resultadoLaboratorio.
     *
     * @param resultadoLaboratorioDTO the resultadoLaboratorioDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new resultadoLaboratorioDTO,
     *         or with status {@code 400 (Bad Request)} if the resultadoLaboratorio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<ResultadoLaboratorioDTO> createResultadoLaboratorio(
            @Valid @RequestBody ResultadoLaboratorioDTO resultadoLaboratorioDTO) throws URISyntaxException {
        LOG.debug("REST request to save ResultadoLaboratorio : {}", resultadoLaboratorioDTO);
        if (resultadoLaboratorioDTO.getId() != null) {
            throw new BadRequestAlertException("A new resultadoLaboratorio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        resultadoLaboratorioDTO = resultadoLaboratorioService.save(resultadoLaboratorioDTO);
        return ResponseEntity.created(new URI("/api/resultados-laboratorio/" + resultadoLaboratorioDTO.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        resultadoLaboratorioDTO.getId().toString()))
                .body(resultadoLaboratorioDTO);
    }

    /**
     * {@code PUT  /resultados-laboratorio/:id} : Updates an existing resultadoLaboratorio.
     *
     * @param id the id of the resultadoLaboratorioDTO to save.
     * @param resultadoLaboratorioDTO the resultadoLaboratorioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated resultadoLaboratorioDTO,
     *         or with status {@code 400 (Bad Request)} if the resultadoLaboratorioDTO is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the resultadoLaboratorioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<ResultadoLaboratorioDTO> updateResultadoLaboratorio(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody ResultadoLaboratorioDTO resultadoLaboratorioDTO) throws URISyntaxException {
        LOG.debug("REST request to update ResultadoLaboratorio : {}, {}", id, resultadoLaboratorioDTO);
        if (resultadoLaboratorioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, resultadoLaboratorioDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!resultadoLaboratorioRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        resultadoLaboratorioDTO = resultadoLaboratorioService.update(resultadoLaboratorioDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        resultadoLaboratorioDTO.getId().toString()))
                .body(resultadoLaboratorioDTO);
    }

    /**
     * {@code GET  /resultados-laboratorio} : get all the resultadoLaboratorios.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of resultadoLaboratorios in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<List<ResultadoLaboratorioDTO>> getAllResultadoLaboratorios(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of ResultadoLaboratorios");
        Page<ResultadoLaboratorioDTO> page = resultadoLaboratorioService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /resultados-laboratorio/:id} : get the "id" resultadoLaboratorio.
     *
     * @param id the id of the resultadoLaboratorioDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the resultadoLaboratorioDTO,
     *         or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<ResultadoLaboratorioDTO> getResultadoLaboratorio(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ResultadoLaboratorio : {}", id);
        Optional<ResultadoLaboratorioDTO> resultadoLaboratorioDTO = resultadoLaboratorioService.findOne(id);
        return ResponseUtil.wrapOrNotFound(resultadoLaboratorioDTO);
    }

    /**
     * {@code GET  /resultados-laboratorio/paciente/:pacienteId} : get all resultadoLaboratorios by paciente.
     *
     * @param pacienteId the id of the paciente.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of resultadoLaboratorios in body.
     */
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<List<ResultadoLaboratorioDTO>> getResultadoLaboratoriosByPaciente(
            @PathVariable("pacienteId") Long pacienteId,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get ResultadoLaboratorios by Paciente : {}", pacienteId);
        Page<ResultadoLaboratorioDTO> page = resultadoLaboratorioService.findByPacienteId(pacienteId, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /resultados-laboratorio/consulta/:consultaId} : get all resultadoLaboratorios by consulta.
     *
     * @param consultaId the id of the consulta.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of resultadoLaboratorios in body.
     */
    @GetMapping("/consulta/{consultaId}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public List<ResultadoLaboratorioDTO> getResultadoLaboratoriosByConsulta(
            @PathVariable("consultaId") Long consultaId) {
        LOG.debug("REST request to get ResultadoLaboratorios by Consulta : {}", consultaId);
        return resultadoLaboratorioService.findByConsultaId(consultaId);
    }

    /**
     * {@code DELETE  /resultados-laboratorio/:id} : delete the "id" resultadoLaboratorio.
     *
     * @param id the id of the resultadoLaboratorioDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteResultadoLaboratorio(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ResultadoLaboratorio : {}", id);
        resultadoLaboratorioService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }
}
