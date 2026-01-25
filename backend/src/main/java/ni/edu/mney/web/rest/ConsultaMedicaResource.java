package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.service.ConsultaMedicaQueryService;
import ni.edu.mney.service.ConsultaMedicaService;
import ni.edu.mney.service.criteria.ConsultaMedicaCriteria;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
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
 * REST controller for managing {@link ni.edu.mney.domain.ConsultaMedica}.
 */
@RestController
@RequestMapping("/api/consulta-medicas")
public class ConsultaMedicaResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultaMedicaResource.class);

    private static final String ENTITY_NAME = "consultaMedica";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ConsultaMedicaService consultaMedicaService;

    private final ConsultaMedicaRepository consultaMedicaRepository;

    private final ConsultaMedicaQueryService consultaMedicaQueryService;

    public ConsultaMedicaResource(
        ConsultaMedicaService consultaMedicaService,
        ConsultaMedicaRepository consultaMedicaRepository,
        ConsultaMedicaQueryService consultaMedicaQueryService
    ) {
        this.consultaMedicaService = consultaMedicaService;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.consultaMedicaQueryService = consultaMedicaQueryService;
    }

    /**
     * {@code POST  /consulta-medicas} : Create a new consultaMedica.
     *
     * @param consultaMedicaDTO the consultaMedicaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new consultaMedicaDTO, or with status {@code 400 (Bad Request)} if the consultaMedica has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ConsultaMedicaDTO> createConsultaMedica(@Valid @RequestBody ConsultaMedicaDTO consultaMedicaDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ConsultaMedica : {}", consultaMedicaDTO);
        if (consultaMedicaDTO.getId() != null) {
            throw new BadRequestAlertException("A new consultaMedica cannot already have an ID", ENTITY_NAME, "idexists");
        }
        consultaMedicaDTO = consultaMedicaService.save(consultaMedicaDTO);
        return ResponseEntity.created(new URI("/api/consulta-medicas/" + consultaMedicaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, consultaMedicaDTO.getId().toString()))
            .body(consultaMedicaDTO);
    }

    /**
     * {@code PUT  /consulta-medicas/:id} : Updates an existing consultaMedica.
     *
     * @param id the id of the consultaMedicaDTO to save.
     * @param consultaMedicaDTO the consultaMedicaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated consultaMedicaDTO,
     * or with status {@code 400 (Bad Request)} if the consultaMedicaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the consultaMedicaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConsultaMedicaDTO> updateConsultaMedica(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ConsultaMedicaDTO consultaMedicaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ConsultaMedica : {}, {}", id, consultaMedicaDTO);
        if (consultaMedicaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, consultaMedicaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!consultaMedicaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        consultaMedicaDTO = consultaMedicaService.update(consultaMedicaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, consultaMedicaDTO.getId().toString()))
            .body(consultaMedicaDTO);
    }

    /**
     * {@code PATCH  /consulta-medicas/:id} : Partial updates given fields of an existing consultaMedica, field will ignore if it is null
     *
     * @param id the id of the consultaMedicaDTO to save.
     * @param consultaMedicaDTO the consultaMedicaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated consultaMedicaDTO,
     * or with status {@code 400 (Bad Request)} if the consultaMedicaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the consultaMedicaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the consultaMedicaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ConsultaMedicaDTO> partialUpdateConsultaMedica(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ConsultaMedicaDTO consultaMedicaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ConsultaMedica partially : {}, {}", id, consultaMedicaDTO);
        if (consultaMedicaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, consultaMedicaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!consultaMedicaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ConsultaMedicaDTO> result = consultaMedicaService.partialUpdate(consultaMedicaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, consultaMedicaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /consulta-medicas} : get all the consultaMedicas.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of consultaMedicas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ConsultaMedicaDTO>> getAllConsultaMedicas(
        ConsultaMedicaCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ConsultaMedicas by criteria: {}", criteria);

        Page<ConsultaMedicaDTO> page = consultaMedicaQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /consulta-medicas/count} : count all the consultaMedicas.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countConsultaMedicas(ConsultaMedicaCriteria criteria) {
        LOG.debug("REST request to count ConsultaMedicas by criteria: {}", criteria);
        return ResponseEntity.ok().body(consultaMedicaQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /consulta-medicas/:id} : get the "id" consultaMedica.
     *
     * @param id the id of the consultaMedicaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the consultaMedicaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultaMedicaDTO> getConsultaMedica(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ConsultaMedica : {}", id);
        Optional<ConsultaMedicaDTO> consultaMedicaDTO = consultaMedicaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(consultaMedicaDTO);
    }

    /**
     * {@code DELETE  /consulta-medicas/:id} : delete the "id" consultaMedica.
     *
     * @param id the id of the consultaMedicaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsultaMedica(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ConsultaMedica : {}", id);
        consultaMedicaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
