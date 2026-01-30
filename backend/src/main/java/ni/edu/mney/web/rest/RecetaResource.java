package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.RecetaRepository;
import ni.edu.mney.service.RecetaQueryService;
import ni.edu.mney.service.RecetaService;
import ni.edu.mney.service.criteria.RecetaCriteria;
import ni.edu.mney.service.dto.RecetaDTO;
import ni.edu.mney.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import ni.edu.mney.security.AuthoritiesConstants;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ni.edu.mney.domain.Receta}.
 */
@RestController
@RequestMapping("/api/recetas")
public class RecetaResource {

    private static final Logger LOG = LoggerFactory.getLogger(RecetaResource.class);

    private static final String ENTITY_NAME = "receta";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RecetaService recetaService;

    private final RecetaRepository recetaRepository;

    private final RecetaQueryService recetaQueryService;

    public RecetaResource(RecetaService recetaService, RecetaRepository recetaRepository,
            RecetaQueryService recetaQueryService) {
        this.recetaService = recetaService;
        this.recetaRepository = recetaRepository;
        this.recetaQueryService = recetaQueryService;
    }

    /**
     * {@code POST  /recetas} : Create a new receta.
     *
     * @param recetaDTO the recetaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new recetaDTO, or with status {@code 400 (Bad Request)} if
     *         the receta has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<RecetaDTO> createReceta(@Valid @RequestBody RecetaDTO recetaDTO) throws URISyntaxException {
        LOG.debug("REST request to save Receta : {}", recetaDTO);
        if (recetaDTO.getId() != null) {
            throw new BadRequestAlertException("A new receta cannot already have an ID", ENTITY_NAME, "idexists");
        }
        recetaDTO = recetaService.save(recetaDTO);
        return ResponseEntity.created(new URI("/api/recetas/" + recetaDTO.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        recetaDTO.getId().toString()))
                .body(recetaDTO);
    }

    /**
     * {@code PUT  /recetas/:id} : Updates an existing receta.
     *
     * @param id        the id of the recetaDTO to save.
     * @param recetaDTO the recetaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated recetaDTO,
     *         or with status {@code 400 (Bad Request)} if the recetaDTO is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the recetaDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<RecetaDTO> updateReceta(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody RecetaDTO recetaDTO) throws URISyntaxException {
        LOG.debug("REST request to update Receta : {}, {}", id, recetaDTO);
        if (recetaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recetaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recetaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        recetaDTO = recetaService.update(recetaDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        recetaDTO.getId().toString()))
                .body(recetaDTO);
    }

    /**
     * {@code PATCH  /recetas/:id} : Partial updates given fields of an existing
     * receta, field will ignore if it is null
     *
     * @param id        the id of the recetaDTO to save.
     * @param recetaDTO the recetaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated recetaDTO,
     *         or with status {@code 400 (Bad Request)} if the recetaDTO is not
     *         valid,
     *         or with status {@code 404 (Not Found)} if the recetaDTO is not found,
     *         or with status {@code 500 (Internal Server Error)} if the recetaDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<RecetaDTO> partialUpdateReceta(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody RecetaDTO recetaDTO) throws URISyntaxException {
        LOG.debug("REST request to partial update Receta partially : {}, {}", id, recetaDTO);
        if (recetaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recetaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recetaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RecetaDTO> result = recetaService.partialUpdate(recetaDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, recetaDTO.getId().toString()));
    }

    /**
     * {@code GET  /recetas} : get all the recetas.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of recetas in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<List<RecetaDTO>> getAllRecetas(
            RecetaCriteria criteria,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get Recetas by criteria: {}", criteria);

        Page<RecetaDTO> page = recetaQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /recetas/count} : count all the recetas.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countRecetas(RecetaCriteria criteria) {
        LOG.debug("REST request to count Recetas by criteria: {}", criteria);
        return ResponseEntity.ok().body(recetaQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /recetas/:id} : get the "id" receta.
     *
     * @param id the id of the recetaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the recetaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<RecetaDTO> getReceta(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Receta : {}", id);
        Optional<RecetaDTO> recetaDTO = recetaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(recetaDTO);
    }

    /**
     * {@code DELETE  /recetas/:id} : delete the "id" receta.
     *
     * @param id the id of the recetaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteReceta(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Receta : {}", id);
        recetaService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }
}
