package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.AuditoriaAccionesRepository;
import ni.edu.mney.service.AuditoriaAccionesQueryService;
import ni.edu.mney.service.AuditoriaAccionesService;
import ni.edu.mney.service.criteria.AuditoriaAccionesCriteria;
import ni.edu.mney.service.dto.AuditoriaAccionesDTO;
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
 * REST controller for managing {@link ni.edu.mney.domain.AuditoriaAcciones}.
 */
@RestController
@RequestMapping("/api/auditoria-acciones")
public class AuditoriaAccionesResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaAccionesResource.class);

    private static final String ENTITY_NAME = "auditoriaAcciones";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuditoriaAccionesService auditoriaAccionesService;

    private final AuditoriaAccionesRepository auditoriaAccionesRepository;

    private final AuditoriaAccionesQueryService auditoriaAccionesQueryService;

    public AuditoriaAccionesResource(
        AuditoriaAccionesService auditoriaAccionesService,
        AuditoriaAccionesRepository auditoriaAccionesRepository,
        AuditoriaAccionesQueryService auditoriaAccionesQueryService
    ) {
        this.auditoriaAccionesService = auditoriaAccionesService;
        this.auditoriaAccionesRepository = auditoriaAccionesRepository;
        this.auditoriaAccionesQueryService = auditoriaAccionesQueryService;
    }

    /**
     * {@code POST  /auditoria-acciones} : Create a new auditoriaAcciones.
     *
     * @param auditoriaAccionesDTO the auditoriaAccionesDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new auditoriaAccionesDTO, or with status {@code 400 (Bad Request)} if the auditoriaAcciones has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AuditoriaAccionesDTO> createAuditoriaAcciones(@Valid @RequestBody AuditoriaAccionesDTO auditoriaAccionesDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save AuditoriaAcciones : {}", auditoriaAccionesDTO);
        if (auditoriaAccionesDTO.getId() != null) {
            throw new BadRequestAlertException("A new auditoriaAcciones cannot already have an ID", ENTITY_NAME, "idexists");
        }
        auditoriaAccionesDTO = auditoriaAccionesService.save(auditoriaAccionesDTO);
        return ResponseEntity.created(new URI("/api/auditoria-acciones/" + auditoriaAccionesDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, auditoriaAccionesDTO.getId().toString()))
            .body(auditoriaAccionesDTO);
    }

    /**
     * {@code PUT  /auditoria-acciones/:id} : Updates an existing auditoriaAcciones.
     *
     * @param id the id of the auditoriaAccionesDTO to save.
     * @param auditoriaAccionesDTO the auditoriaAccionesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated auditoriaAccionesDTO,
     * or with status {@code 400 (Bad Request)} if the auditoriaAccionesDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the auditoriaAccionesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuditoriaAccionesDTO> updateAuditoriaAcciones(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AuditoriaAccionesDTO auditoriaAccionesDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AuditoriaAcciones : {}, {}", id, auditoriaAccionesDTO);
        if (auditoriaAccionesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, auditoriaAccionesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!auditoriaAccionesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        auditoriaAccionesDTO = auditoriaAccionesService.update(auditoriaAccionesDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, auditoriaAccionesDTO.getId().toString()))
            .body(auditoriaAccionesDTO);
    }

    /**
     * {@code PATCH  /auditoria-acciones/:id} : Partial updates given fields of an existing auditoriaAcciones, field will ignore if it is null
     *
     * @param id the id of the auditoriaAccionesDTO to save.
     * @param auditoriaAccionesDTO the auditoriaAccionesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated auditoriaAccionesDTO,
     * or with status {@code 400 (Bad Request)} if the auditoriaAccionesDTO is not valid,
     * or with status {@code 404 (Not Found)} if the auditoriaAccionesDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the auditoriaAccionesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AuditoriaAccionesDTO> partialUpdateAuditoriaAcciones(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AuditoriaAccionesDTO auditoriaAccionesDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AuditoriaAcciones partially : {}, {}", id, auditoriaAccionesDTO);
        if (auditoriaAccionesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, auditoriaAccionesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!auditoriaAccionesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AuditoriaAccionesDTO> result = auditoriaAccionesService.partialUpdate(auditoriaAccionesDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, auditoriaAccionesDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /auditoria-acciones} : get all the auditoriaAcciones.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auditoriaAcciones in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AuditoriaAccionesDTO>> getAllAuditoriaAcciones(
        AuditoriaAccionesCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get AuditoriaAcciones by criteria: {}", criteria);

        Page<AuditoriaAccionesDTO> page = auditoriaAccionesQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /auditoria-acciones/count} : count all the auditoriaAcciones.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAuditoriaAcciones(AuditoriaAccionesCriteria criteria) {
        LOG.debug("REST request to count AuditoriaAcciones by criteria: {}", criteria);
        return ResponseEntity.ok().body(auditoriaAccionesQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /auditoria-acciones/:id} : get the "id" auditoriaAcciones.
     *
     * @param id the id of the auditoriaAccionesDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auditoriaAccionesDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditoriaAccionesDTO> getAuditoriaAcciones(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AuditoriaAcciones : {}", id);
        Optional<AuditoriaAccionesDTO> auditoriaAccionesDTO = auditoriaAccionesService.findOne(id);
        return ResponseUtil.wrapOrNotFound(auditoriaAccionesDTO);
    }

    /**
     * {@code DELETE  /auditoria-acciones/:id} : delete the "id" auditoriaAcciones.
     *
     * @param id the id of the auditoriaAccionesDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditoriaAcciones(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AuditoriaAcciones : {}", id);
        auditoriaAccionesService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
