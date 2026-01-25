package ni.edu.mney.service;

import jakarta.persistence.criteria.JoinType;
import ni.edu.mney.domain.*; // for static metamodels
import ni.edu.mney.domain.AuditoriaAcciones;
import ni.edu.mney.repository.AuditoriaAccionesRepository;
import ni.edu.mney.service.criteria.AuditoriaAccionesCriteria;
import ni.edu.mney.service.dto.AuditoriaAccionesDTO;
import ni.edu.mney.service.mapper.AuditoriaAccionesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link AuditoriaAcciones} entities in the database.
 * The main input is a {@link AuditoriaAccionesCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AuditoriaAccionesDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AuditoriaAccionesQueryService extends QueryService<AuditoriaAcciones> {

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaAccionesQueryService.class);

    private final AuditoriaAccionesRepository auditoriaAccionesRepository;

    private final AuditoriaAccionesMapper auditoriaAccionesMapper;

    public AuditoriaAccionesQueryService(
        AuditoriaAccionesRepository auditoriaAccionesRepository,
        AuditoriaAccionesMapper auditoriaAccionesMapper
    ) {
        this.auditoriaAccionesRepository = auditoriaAccionesRepository;
        this.auditoriaAccionesMapper = auditoriaAccionesMapper;
    }

    /**
     * Return a {@link Page} of {@link AuditoriaAccionesDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AuditoriaAccionesDTO> findByCriteria(AuditoriaAccionesCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AuditoriaAcciones> specification = createSpecification(criteria);
        return auditoriaAccionesRepository.findAll(specification, page).map(auditoriaAccionesMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AuditoriaAccionesCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<AuditoriaAcciones> specification = createSpecification(criteria);
        return auditoriaAccionesRepository.count(specification);
    }

    /**
     * Function to convert {@link AuditoriaAccionesCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AuditoriaAcciones> createSpecification(AuditoriaAccionesCriteria criteria) {
        Specification<AuditoriaAcciones> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), AuditoriaAcciones_.id),
                buildStringSpecification(criteria.getEntidad(), AuditoriaAcciones_.entidad),
                buildStringSpecification(criteria.getAccion(), AuditoriaAcciones_.accion),
                buildRangeSpecification(criteria.getFecha(), AuditoriaAcciones_.fecha),
                buildStringSpecification(criteria.getDescripcion(), AuditoriaAcciones_.descripcion),
                buildSpecification(criteria.getUserId(), root -> root.join(AuditoriaAcciones_.user, JoinType.LEFT).get(User_.id))
            );
        }
        return specification;
    }
}
