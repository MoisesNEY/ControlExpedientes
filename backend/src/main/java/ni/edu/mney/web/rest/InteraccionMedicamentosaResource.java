package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.InteraccionMedicamentosaRepository;
import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.InteraccionMedicamentosaService;
import ni.edu.mney.service.dto.InteraccionMedicamentosaDTO;
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
 * REST controller for managing {@link ni.edu.mney.domain.InteraccionMedicamentosa}.
 */
@RestController
@RequestMapping("/api/interacciones-medicamentosas")
public class InteraccionMedicamentosaResource {

    private static final Logger LOG = LoggerFactory.getLogger(InteraccionMedicamentosaResource.class);

    private static final String ENTITY_NAME = "interaccionMedicamentosa";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InteraccionMedicamentosaService interaccionMedicamentosaService;

    private final InteraccionMedicamentosaRepository interaccionMedicamentosaRepository;

    public InteraccionMedicamentosaResource(
        InteraccionMedicamentosaService interaccionMedicamentosaService,
        InteraccionMedicamentosaRepository interaccionMedicamentosaRepository
    ) {
        this.interaccionMedicamentosaService = interaccionMedicamentosaService;
        this.interaccionMedicamentosaRepository = interaccionMedicamentosaRepository;
    }

    /**
     * {@code POST  /interacciones-medicamentosas} : Create a new interaccionMedicamentosa.
     *
     * @param interaccionMedicamentosaDTO the interaccionMedicamentosaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new interaccionMedicamentosaDTO,
     *         or with status {@code 400 (Bad Request)} if the interaccionMedicamentosa has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<InteraccionMedicamentosaDTO> createInteraccionMedicamentosa(
        @Valid @RequestBody InteraccionMedicamentosaDTO interaccionMedicamentosaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save InteraccionMedicamentosa : {}", interaccionMedicamentosaDTO);
        if (interaccionMedicamentosaDTO.getId() != null) {
            throw new BadRequestAlertException("A new interaccionMedicamentosa cannot already have an ID", ENTITY_NAME, "idexists");
        }
        interaccionMedicamentosaDTO = interaccionMedicamentosaService.save(interaccionMedicamentosaDTO);
        return ResponseEntity.created(new URI("/api/interacciones-medicamentosas/" + interaccionMedicamentosaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                interaccionMedicamentosaDTO.getId().toString()))
            .body(interaccionMedicamentosaDTO);
    }

    /**
     * {@code PUT  /interacciones-medicamentosas/:id} : Updates an existing interaccionMedicamentosa.
     *
     * @param id the id of the interaccionMedicamentosaDTO to save.
     * @param interaccionMedicamentosaDTO the interaccionMedicamentosaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated interaccionMedicamentosaDTO,
     *         or with status {@code 400 (Bad Request)} if the interaccionMedicamentosaDTO is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the interaccionMedicamentosaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<InteraccionMedicamentosaDTO> updateInteraccionMedicamentosa(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody InteraccionMedicamentosaDTO interaccionMedicamentosaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update InteraccionMedicamentosa : {}, {}", id, interaccionMedicamentosaDTO);
        if (interaccionMedicamentosaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, interaccionMedicamentosaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!interaccionMedicamentosaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        interaccionMedicamentosaDTO = interaccionMedicamentosaService.update(interaccionMedicamentosaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                interaccionMedicamentosaDTO.getId().toString()))
            .body(interaccionMedicamentosaDTO);
    }

    /**
     * {@code GET  /interacciones-medicamentosas} : get all the interaccionMedicamentosas.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of interaccionMedicamentosas in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<List<InteraccionMedicamentosaDTO>> getAllInteraccionMedicamentosas(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of InteraccionMedicamentosas");
        Page<InteraccionMedicamentosaDTO> page = interaccionMedicamentosaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
            .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /interacciones-medicamentosas/:id} : get the "id" interaccionMedicamentosa.
     *
     * @param id the id of the interaccionMedicamentosaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the interaccionMedicamentosaDTO,
     *         or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<InteraccionMedicamentosaDTO> getInteraccionMedicamentosa(@PathVariable("id") Long id) {
        LOG.debug("REST request to get InteraccionMedicamentosa : {}", id);
        Optional<InteraccionMedicamentosaDTO> interaccionMedicamentosaDTO = interaccionMedicamentosaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(interaccionMedicamentosaDTO);
    }

    /**
     * {@code POST  /interacciones-medicamentosas/verificar} : verify drug interactions.
     * Given a list of medication IDs (being prescribed), returns all known interactions among them.
     *
     * @param medicamentoIds the list of medication IDs to check.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of interactions found.
     */
    @PostMapping("/verificar")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<List<InteraccionMedicamentosaDTO>> verificarInteracciones(
        @RequestBody List<Long> medicamentoIds
    ) {
        LOG.debug("REST request to verify drug interactions for IDs : {}", medicamentoIds);
        List<InteraccionMedicamentosaDTO> result = interaccionMedicamentosaService.verificarInteracciones(medicamentoIds);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code DELETE  /interacciones-medicamentosas/:id} : delete the "id" interaccionMedicamentosa.
     *
     * @param id the id of the interaccionMedicamentosaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteInteraccionMedicamentosa(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete InteraccionMedicamentosa : {}", id);
        interaccionMedicamentosaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
