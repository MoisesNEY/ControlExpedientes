package ni.edu.mney.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.repository.SignosVitalesRepository;
import ni.edu.mney.service.SignosVitalesQueryService;
import ni.edu.mney.service.SignosVitalesService;
import ni.edu.mney.service.criteria.SignosVitalesCriteria;
import ni.edu.mney.service.dto.SignosVitalesDTO;
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
 * REST controller for managing {@link ni.edu.mney.domain.SignosVitales}.
 */
@RestController
@RequestMapping("/api/signos-vitales")
public class SignosVitalesResource {

    private static final Logger LOG = LoggerFactory.getLogger(SignosVitalesResource.class);

    private static final String ENTITY_NAME = "signosVitales";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SignosVitalesService signosVitalesService;

    private final SignosVitalesRepository signosVitalesRepository;

    private final SignosVitalesQueryService signosVitalesQueryService;

    public SignosVitalesResource(
            SignosVitalesService signosVitalesService,
            SignosVitalesRepository signosVitalesRepository,
            SignosVitalesQueryService signosVitalesQueryService) {
        this.signosVitalesService = signosVitalesService;
        this.signosVitalesRepository = signosVitalesRepository;
        this.signosVitalesQueryService = signosVitalesQueryService;
    }

    /**
     * {@code POST  /signos-vitales} : Create a new signosVitales.
     *
     * @param signosVitalesDTO the signosVitalesDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new signosVitalesDTO, or with status
     *         {@code 400 (Bad Request)} if the signosVitales has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<SignosVitalesDTO> createSignosVitales(@RequestBody SignosVitalesDTO signosVitalesDTO)
            throws URISyntaxException {
        LOG.debug("REST request to save SignosVitales : {}", signosVitalesDTO);
        if (signosVitalesDTO.getId() != null) {
            throw new BadRequestAlertException("A new signosVitales cannot already have an ID", ENTITY_NAME,
                    "idexists");
        }
        signosVitalesDTO = signosVitalesService.save(signosVitalesDTO);
        return ResponseEntity.created(new URI("/api/signos-vitales/" + signosVitalesDTO.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        signosVitalesDTO.getId().toString()))
                .body(signosVitalesDTO);
    }

    /**
     * {@code PUT  /signos-vitales/:id} : Updates an existing signosVitales.
     *
     * @param id               the id of the signosVitalesDTO to save.
     * @param signosVitalesDTO the signosVitalesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated signosVitalesDTO,
     *         or with status {@code 400 (Bad Request)} if the signosVitalesDTO is
     *         not valid,
     *         or with status {@code 500 (Internal Server Error)} if the
     *         signosVitalesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<SignosVitalesDTO> updateSignosVitales(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody SignosVitalesDTO signosVitalesDTO) throws URISyntaxException {
        LOG.debug("REST request to update SignosVitales : {}, {}", id, signosVitalesDTO);
        if (signosVitalesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, signosVitalesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!signosVitalesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        signosVitalesDTO = signosVitalesService.update(signosVitalesDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        signosVitalesDTO.getId().toString()))
                .body(signosVitalesDTO);
    }

    /**
     * {@code PATCH  /signos-vitales/:id} : Partial updates given fields of an
     * existing signosVitales, field will ignore if it is null
     *
     * @param id               the id of the signosVitalesDTO to save.
     * @param signosVitalesDTO the signosVitalesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated signosVitalesDTO,
     *         or with status {@code 400 (Bad Request)} if the signosVitalesDTO is
     *         not valid,
     *         or with status {@code 404 (Not Found)} if the signosVitalesDTO is not
     *         found,
     *         or with status {@code 500 (Internal Server Error)} if the
     *         signosVitalesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<SignosVitalesDTO> partialUpdateSignosVitales(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody SignosVitalesDTO signosVitalesDTO) throws URISyntaxException {
        LOG.debug("REST request to partial update SignosVitales partially : {}, {}", id, signosVitalesDTO);
        if (signosVitalesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, signosVitalesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!signosVitalesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SignosVitalesDTO> result = signosVitalesService.partialUpdate(signosVitalesDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        signosVitalesDTO.getId().toString()));
    }

    /**
     * {@code GET  /signos-vitales} : get all the signosVitales.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of signosVitales in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<List<SignosVitalesDTO>> getAllSignosVitales(
            SignosVitalesCriteria criteria,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get SignosVitales by criteria: {}", criteria);

        Page<SignosVitalesDTO> page = signosVitalesQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /signos-vitales/count} : count all the signosVitales.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSignosVitales(SignosVitalesCriteria criteria) {
        LOG.debug("REST request to count SignosVitales by criteria: {}", criteria);
        return ResponseEntity.ok().body(signosVitalesQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /signos-vitales/:id} : get the "id" signosVitales.
     *
     * @param id the id of the signosVitalesDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the signosVitalesDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<SignosVitalesDTO> getSignosVitales(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SignosVitales : {}", id);
        Optional<SignosVitalesDTO> signosVitalesDTO = signosVitalesService.findOne(id);
        return ResponseUtil.wrapOrNotFound(signosVitalesDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteSignosVitales(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SignosVitales : {}", id);
        signosVitalesService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }

    /**
     * {@code GET  /signos-vitales/paciente/:pacienteId/hoy} : get today's
     * signosVitales by pacienteId.
     *
     * @param pacienteId the id of the paciente.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of signosVitales in body.
     */
    @GetMapping("/paciente/{pacienteId}/hoy")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "', '"
            + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<List<SignosVitalesDTO>> getSignosVitalesByPacienteHoy(
            @PathVariable("pacienteId") Long pacienteId) {
        LOG.debug("REST request to get today's SignosVitales for Paciente : {}", pacienteId);
        List<SignosVitalesDTO> result = signosVitalesService.findLatestByPacienteIdAndDate(pacienteId,
                java.time.LocalDate.now());
        return ResponseEntity.ok().body(result);
    }
}
