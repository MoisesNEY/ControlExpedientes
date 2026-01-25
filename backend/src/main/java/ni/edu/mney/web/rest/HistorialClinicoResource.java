package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.HistorialClinicoRepository;
import ni.edu.mney.service.HistorialClinicoQueryService;
import ni.edu.mney.service.HistorialClinicoService;
import ni.edu.mney.service.criteria.HistorialClinicoCriteria;
import ni.edu.mney.service.dto.HistorialClinicoDTO;
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
 * REST controller for managing {@link ni.edu.mney.domain.HistorialClinico}.
 */
@RestController
@RequestMapping("/api/historial-clinicos")
public class HistorialClinicoResource {

    private static final Logger LOG = LoggerFactory.getLogger(HistorialClinicoResource.class);

    private static final String ENTITY_NAME = "historialClinico";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final HistorialClinicoService historialClinicoService;

    private final HistorialClinicoRepository historialClinicoRepository;

    private final HistorialClinicoQueryService historialClinicoQueryService;

    public HistorialClinicoResource(
        HistorialClinicoService historialClinicoService,
        HistorialClinicoRepository historialClinicoRepository,
        HistorialClinicoQueryService historialClinicoQueryService
    ) {
        this.historialClinicoService = historialClinicoService;
        this.historialClinicoRepository = historialClinicoRepository;
        this.historialClinicoQueryService = historialClinicoQueryService;
    }

    /**
     * {@code POST  /historial-clinicos} : Create a new historialClinico.
     *
     * @param historialClinicoDTO the historialClinicoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new historialClinicoDTO, or with status {@code 400 (Bad Request)} if the historialClinico has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<HistorialClinicoDTO> createHistorialClinico(@Valid @RequestBody HistorialClinicoDTO historialClinicoDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save HistorialClinico : {}", historialClinicoDTO);
        if (historialClinicoDTO.getId() != null) {
            throw new BadRequestAlertException("A new historialClinico cannot already have an ID", ENTITY_NAME, "idexists");
        }
        historialClinicoDTO = historialClinicoService.save(historialClinicoDTO);
        return ResponseEntity.created(new URI("/api/historial-clinicos/" + historialClinicoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, historialClinicoDTO.getId().toString()))
            .body(historialClinicoDTO);
    }

    /**
     * {@code PUT  /historial-clinicos/:id} : Updates an existing historialClinico.
     *
     * @param id the id of the historialClinicoDTO to save.
     * @param historialClinicoDTO the historialClinicoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated historialClinicoDTO,
     * or with status {@code 400 (Bad Request)} if the historialClinicoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the historialClinicoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HistorialClinicoDTO> updateHistorialClinico(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody HistorialClinicoDTO historialClinicoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update HistorialClinico : {}, {}", id, historialClinicoDTO);
        if (historialClinicoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, historialClinicoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!historialClinicoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        historialClinicoDTO = historialClinicoService.update(historialClinicoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, historialClinicoDTO.getId().toString()))
            .body(historialClinicoDTO);
    }

    /**
     * {@code PATCH  /historial-clinicos/:id} : Partial updates given fields of an existing historialClinico, field will ignore if it is null
     *
     * @param id the id of the historialClinicoDTO to save.
     * @param historialClinicoDTO the historialClinicoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated historialClinicoDTO,
     * or with status {@code 400 (Bad Request)} if the historialClinicoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the historialClinicoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the historialClinicoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<HistorialClinicoDTO> partialUpdateHistorialClinico(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody HistorialClinicoDTO historialClinicoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update HistorialClinico partially : {}, {}", id, historialClinicoDTO);
        if (historialClinicoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, historialClinicoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!historialClinicoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<HistorialClinicoDTO> result = historialClinicoService.partialUpdate(historialClinicoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, historialClinicoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /historial-clinicos} : get all the historialClinicos.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of historialClinicos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<HistorialClinicoDTO>> getAllHistorialClinicos(
        HistorialClinicoCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get HistorialClinicos by criteria: {}", criteria);

        Page<HistorialClinicoDTO> page = historialClinicoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /historial-clinicos/count} : count all the historialClinicos.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countHistorialClinicos(HistorialClinicoCriteria criteria) {
        LOG.debug("REST request to count HistorialClinicos by criteria: {}", criteria);
        return ResponseEntity.ok().body(historialClinicoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /historial-clinicos/:id} : get the "id" historialClinico.
     *
     * @param id the id of the historialClinicoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the historialClinicoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistorialClinicoDTO> getHistorialClinico(@PathVariable("id") Long id) {
        LOG.debug("REST request to get HistorialClinico : {}", id);
        Optional<HistorialClinicoDTO> historialClinicoDTO = historialClinicoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(historialClinicoDTO);
    }

    /**
     * {@code DELETE  /historial-clinicos/:id} : delete the "id" historialClinico.
     *
     * @param id the id of the historialClinicoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorialClinico(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete HistorialClinico : {}", id);
        historialClinicoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
