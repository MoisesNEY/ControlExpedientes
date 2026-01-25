package ni.edu.mney.service;

import jakarta.persistence.criteria.JoinType;
import ni.edu.mney.domain.*; // for static metamodels
import ni.edu.mney.domain.SignosVitales;
import ni.edu.mney.repository.SignosVitalesRepository;
import ni.edu.mney.service.criteria.SignosVitalesCriteria;
import ni.edu.mney.service.dto.SignosVitalesDTO;
import ni.edu.mney.service.mapper.SignosVitalesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link SignosVitales} entities in the database.
 * The main input is a {@link SignosVitalesCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link SignosVitalesDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SignosVitalesQueryService extends QueryService<SignosVitales> {

    private static final Logger LOG = LoggerFactory.getLogger(SignosVitalesQueryService.class);

    private final SignosVitalesRepository signosVitalesRepository;

    private final SignosVitalesMapper signosVitalesMapper;

    public SignosVitalesQueryService(SignosVitalesRepository signosVitalesRepository, SignosVitalesMapper signosVitalesMapper) {
        this.signosVitalesRepository = signosVitalesRepository;
        this.signosVitalesMapper = signosVitalesMapper;
    }

    /**
     * Return a {@link Page} of {@link SignosVitalesDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SignosVitalesDTO> findByCriteria(SignosVitalesCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<SignosVitales> specification = createSpecification(criteria);
        return signosVitalesRepository.findAll(specification, page).map(signosVitalesMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SignosVitalesCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<SignosVitales> specification = createSpecification(criteria);
        return signosVitalesRepository.count(specification);
    }

    /**
     * Function to convert {@link SignosVitalesCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SignosVitales> createSpecification(SignosVitalesCriteria criteria) {
        Specification<SignosVitales> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), SignosVitales_.id),
                buildRangeSpecification(criteria.getPeso(), SignosVitales_.peso),
                buildRangeSpecification(criteria.getAltura(), SignosVitales_.altura),
                buildStringSpecification(criteria.getPresionArterial(), SignosVitales_.presionArterial),
                buildRangeSpecification(criteria.getTemperatura(), SignosVitales_.temperatura),
                buildRangeSpecification(criteria.getFrecuenciaCardiaca(), SignosVitales_.frecuenciaCardiaca),
                buildSpecification(criteria.getConsultaId(), root ->
                    root.join(SignosVitales_.consulta, JoinType.LEFT).get(ConsultaMedica_.id)
                )
            );
        }
        return specification;
    }
}
