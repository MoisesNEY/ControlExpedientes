package ni.edu.mney.service;

import jakarta.persistence.criteria.JoinType;
import ni.edu.mney.domain.*; // for static metamodels
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.service.criteria.CitaMedicaCriteria;
import ni.edu.mney.service.dto.CitaMedicaDTO;
import ni.edu.mney.service.mapper.CitaMedicaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link CitaMedica} entities in the
 * database.
 * The main input is a {@link CitaMedicaCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link CitaMedicaDTO} which fulfills the
 * criteria.
 */
@Service
@Transactional(readOnly = true)
public class CitaMedicaQueryService extends QueryService<CitaMedica> {

    private static final Logger LOG = LoggerFactory.getLogger(CitaMedicaQueryService.class);

    private final CitaMedicaRepository citaMedicaRepository;

    private final CitaMedicaMapper citaMedicaMapper;

    public CitaMedicaQueryService(CitaMedicaRepository citaMedicaRepository, CitaMedicaMapper citaMedicaMapper) {
        this.citaMedicaRepository = citaMedicaRepository;
        this.citaMedicaMapper = citaMedicaMapper;
    }

    /**
     * Return a {@link Page} of {@link CitaMedicaDTO} which matches the criteria
     * from the database.
     * 
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CitaMedicaDTO> findByCriteria(CitaMedicaCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CitaMedica> specification = createSpecification(criteria);
        return citaMedicaRepository.findAll(specification, page).map(citaMedicaMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * 
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CitaMedicaCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<CitaMedica> specification = createSpecification(criteria);
        return citaMedicaRepository.count(specification);
    }

    /**
     * Function to convert {@link CitaMedicaCriteria} to a {@link Specification}
     * 
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CitaMedica> createSpecification(CitaMedicaCriteria criteria) {
        Specification<CitaMedica> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                    buildRangeSpecification(criteria.getId(), CitaMedica_.id),
                    buildRangeSpecification(criteria.getFechaHora(), CitaMedica_.fechaHora),
                    buildSpecification(criteria.getEstado(), CitaMedica_.estado),
                    buildStringSpecification(criteria.getObservaciones(), CitaMedica_.observaciones),
                    buildSpecification(criteria.getUserId(),
                            root -> root.join(CitaMedica_.user, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getPacienteId(),
                            root -> root.join(CitaMedica_.paciente, JoinType.LEFT).get(Paciente_.id)));
        }
        return specification;
    }
}
