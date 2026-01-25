package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.DiagnosticoRepository;
import ni.edu.mney.service.DiagnosticoQueryService;
import ni.edu.mney.service.DiagnosticoService;
import ni.edu.mney.service.criteria.DiagnosticoCriteria;
import ni.edu.mney.service.dto.DiagnosticoDTO;
import ni.edu.mney.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ni.edu.mney.domain.Diagnostico}.
 */
@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoResource {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticoResource.class);

    private static final String ENTITY_NAME = "diagnostico";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DiagnosticoService diagnosticoService;

    private final DiagnosticoRepository diagnosticoRepository;

    private final DiagnosticoQueryService diagnosticoQueryService;

    public DiagnosticoResource(
        DiagnosticoService diagnosticoService,
        DiagnosticoRepository diagnosticoRepository,
        DiagnosticoQueryService diagnosticoQueryService
    ) {
        this.diagnosticoService = diagnosticoService;
        this.diagnosticoRepository = diagnosticoRepository;
        this.diagnosticoQueryService = diagnosticoQueryService;
    }

    /**
     * {@code POST  /diagnosticos} : Create a new diagnostico.
     *
     * @param diagnosticoDTO the diagnosticoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new diagnosticoDTO, or with status {@code 400 (Bad Request)} if the diagnostico has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DiagnosticoDTO> createDiagnostico(@Valid @RequestBody DiagnosticoDTO diagnosticoDTO) throws URISyntaxException {
        LOG.debug("REST request to save Diagnostico : {}", diagnosticoDTO);
        if (diagnosticoDTO.getId() != null) {
            throw new BadRequestAlertException("A new diagnostico cannot already have an ID", ENTITY_NAME, "idexists");
        }
        diagnosticoDTO = diagnosticoService.save(diagnosticoDTO);
        return ResponseEntity.created(new URI("/api/diagnosticos/" + diagnosticoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, diagnosticoDTO.getId().toString()))
            .body(diagnosticoDTO);
    }

    /**
     * {@code PUT  /diagnosticos/:id} : Updates an existing diagnostico.
     *
     * @param id the id of the diagnosticoDTO to save.
     * @param diagnosticoDTO the diagnosticoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated diagnosticoDTO,
     * or with status {@code 400 (Bad Request)} if the diagnosticoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the diagnosticoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticoDTO> updateDiagnostico(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DiagnosticoDTO diagnosticoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Diagnostico : {}, {}", id, diagnosticoDTO);
        if (diagnosticoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, diagnosticoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!diagnosticoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        diagnosticoDTO = diagnosticoService.update(diagnosticoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, diagnosticoDTO.getId().toString()))
            .body(diagnosticoDTO);
    }

    /**
     * {@code PATCH  /diagnosticos/:id} : Partial updates given fields of an existing diagnostico, field will ignore if it is null
     *
     * @param id the id of the diagnosticoDTO to save.
     * @param diagnosticoDTO the diagnosticoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated diagnosticoDTO,
     * or with status {@code 400 (Bad Request)} if the diagnosticoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the diagnosticoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the diagnosticoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DiagnosticoDTO> partialUpdateDiagnostico(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DiagnosticoDTO diagnosticoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Diagnostico partially : {}, {}", id, diagnosticoDTO);
        if (diagnosticoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, diagnosticoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!diagnosticoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DiagnosticoDTO> result = diagnosticoService.partialUpdate(diagnosticoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, diagnosticoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /diagnosticos} : get all the diagnosticos.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of diagnosticos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DiagnosticoDTO>> getAllDiagnosticos(
        DiagnosticoCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Diagnosticos by criteria: {}", criteria);

        Page<DiagnosticoDTO> page = diagnosticoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /diagnosticos/count} : count all the diagnosticos.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countDiagnosticos(DiagnosticoCriteria criteria) {
        LOG.debug("REST request to count Diagnosticos by criteria: {}", criteria);
        return ResponseEntity.ok().body(diagnosticoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /diagnosticos/:id} : get the "id" diagnostico.
     *
     * @param id the id of the diagnosticoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the diagnosticoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticoDTO> getDiagnostico(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Diagnostico : {}", id);
        Optional<DiagnosticoDTO> diagnosticoDTO = diagnosticoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(diagnosticoDTO);
    }

    /**
     * {@code DELETE  /diagnosticos/:id} : delete the "id" diagnostico.
     *
     * @param id the id of the diagnosticoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagnostico(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Diagnostico : {}", id);
        diagnosticoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
