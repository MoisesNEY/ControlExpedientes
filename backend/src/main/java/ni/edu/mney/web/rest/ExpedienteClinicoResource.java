package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.service.ExpedienteClinicoQueryService;
import ni.edu.mney.service.ExpedienteClinicoService;
import ni.edu.mney.service.criteria.ExpedienteClinicoCriteria;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
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
 * REST controller for managing {@link ni.edu.mney.domain.ExpedienteClinico}.
 */
@RestController
@RequestMapping("/api/expediente-clinicos")
@PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
public class ExpedienteClinicoResource {

    private static final Logger LOG = LoggerFactory.getLogger(ExpedienteClinicoResource.class);

    private static final String ENTITY_NAME = "expedienteClinico";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ExpedienteClinicoService expedienteClinicoService;

    private final ExpedienteClinicoRepository expedienteClinicoRepository;

    private final ExpedienteClinicoQueryService expedienteClinicoQueryService;

    public ExpedienteClinicoResource(
            ExpedienteClinicoService expedienteClinicoService,
            ExpedienteClinicoRepository expedienteClinicoRepository,
            ExpedienteClinicoQueryService expedienteClinicoQueryService) {
        this.expedienteClinicoService = expedienteClinicoService;
        this.expedienteClinicoRepository = expedienteClinicoRepository;
        this.expedienteClinicoQueryService = expedienteClinicoQueryService;
    }

    /**
     * {@code POST  /expediente-clinicos} : Create a new expedienteClinico.
     *
     * @param expedienteClinicoDTO the expedienteClinicoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new expedienteClinicoDTO, or with status
     *         {@code 400 (Bad Request)} if the expedienteClinico has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ExpedienteClinicoDTO> createExpedienteClinico(
            @Valid @RequestBody ExpedienteClinicoDTO expedienteClinicoDTO)
            throws URISyntaxException {
        LOG.debug("REST request to save ExpedienteClinico : {}", expedienteClinicoDTO);
        if (expedienteClinicoDTO.getId() != null) {
            throw new BadRequestAlertException("A new expedienteClinico cannot already have an ID", ENTITY_NAME,
                    "idexists");
        }
        expedienteClinicoDTO = expedienteClinicoService.save(expedienteClinicoDTO);
        return ResponseEntity.created(new URI("/api/expediente-clinicos/" + expedienteClinicoDTO.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        expedienteClinicoDTO.getId().toString()))
                .body(expedienteClinicoDTO);
    }

    /**
     * {@code PUT  /expediente-clinicos/:id} : Updates an existing
     * expedienteClinico.
     *
     * @param id                   the id of the expedienteClinicoDTO to save.
     * @param expedienteClinicoDTO the expedienteClinicoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated expedienteClinicoDTO,
     *         or with status {@code 400 (Bad Request)} if the expedienteClinicoDTO
     *         is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the
     *         expedienteClinicoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpedienteClinicoDTO> updateExpedienteClinico(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody ExpedienteClinicoDTO expedienteClinicoDTO) throws URISyntaxException {
        LOG.debug("REST request to update ExpedienteClinico : {}, {}", id, expedienteClinicoDTO);
        if (expedienteClinicoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, expedienteClinicoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!expedienteClinicoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        expedienteClinicoDTO = expedienteClinicoService.update(expedienteClinicoDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        expedienteClinicoDTO.getId().toString()))
                .body(expedienteClinicoDTO);
    }

    /**
     * {@code PATCH  /expediente-clinicos/:id} : Partial updates given fields of an
     * existing expedienteClinico, field will ignore if it is null
     *
     * @param id                   the id of the expedienteClinicoDTO to save.
     * @param expedienteClinicoDTO the expedienteClinicoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated expedienteClinicoDTO,
     *         or with status {@code 400 (Bad Request)} if the expedienteClinicoDTO
     *         is not valid,
     *         or with status {@code 404 (Not Found)} if the expedienteClinicoDTO is
     *         not found,
     *         or with status {@code 500 (Internal Server Error)} if the
     *         expedienteClinicoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ExpedienteClinicoDTO> partialUpdateExpedienteClinico(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody ExpedienteClinicoDTO expedienteClinicoDTO) throws URISyntaxException {
        LOG.debug("REST request to partial update ExpedienteClinico partially : {}, {}", id, expedienteClinicoDTO);
        if (expedienteClinicoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, expedienteClinicoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!expedienteClinicoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ExpedienteClinicoDTO> result = expedienteClinicoService.partialUpdate(expedienteClinicoDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        expedienteClinicoDTO.getId().toString()));
    }

    /**
     * {@code GET  /expediente-clinicos} : get all the expedienteClinicos.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of expedienteClinicos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ExpedienteClinicoDTO>> getAllExpedienteClinicos(
            ExpedienteClinicoCriteria criteria,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get ExpedienteClinicos by criteria: {}", criteria);

        Page<ExpedienteClinicoDTO> page = expedienteClinicoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /expediente-clinicos/count} : count all the expedienteClinicos.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countExpedienteClinicos(ExpedienteClinicoCriteria criteria) {
        LOG.debug("REST request to count ExpedienteClinicos by criteria: {}", criteria);
        return ResponseEntity.ok().body(expedienteClinicoQueryService.countByCriteria(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpedienteClinicoDTO> getExpedienteClinico(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ExpedienteClinico : {}", id);
        Optional<ExpedienteClinicoDTO> expedienteClinicoDTO = expedienteClinicoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(expedienteClinicoDTO);
    }

    /**
     * {@code GET  /expediente-clinicos/paciente/:id} : get the expedienteClinico by
     * paciente "id".
     *
     * @param id the id of the paciente to retrieve the expediente for.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the expedienteClinicoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/paciente/{id}")
    public ResponseEntity<ExpedienteClinicoDTO> getExpedienteByPaciente(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ExpedienteClinico by paciente id : {}", id);
        Optional<ExpedienteClinicoDTO> expedienteClinicoDTO = expedienteClinicoService.findByPacienteId(id);
        return ResponseUtil.wrapOrNotFound(expedienteClinicoDTO);
    }

    /**
     * {@code DELETE  /expediente-clinicos/:id} : delete the "id" expedienteClinico.
     *
     * @param id the id of the expedienteClinicoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpedienteClinico(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ExpedienteClinico : {}", id);
        expedienteClinicoService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }
}
