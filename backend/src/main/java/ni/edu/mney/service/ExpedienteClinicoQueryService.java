package ni.edu.mney.service;

import jakarta.persistence.criteria.JoinType;
import ni.edu.mney.domain.*; // for static metamodels
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.service.criteria.ExpedienteClinicoCriteria;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.mapper.ExpedienteClinicoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link ExpedienteClinico} entities in the database.
 * The main input is a {@link ExpedienteClinicoCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ExpedienteClinicoDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ExpedienteClinicoQueryService extends QueryService<ExpedienteClinico> {

    private static final Logger LOG = LoggerFactory.getLogger(ExpedienteClinicoQueryService.class);

    private final ExpedienteClinicoRepository expedienteClinicoRepository;

    private final ExpedienteClinicoMapper expedienteClinicoMapper;

    public ExpedienteClinicoQueryService(
        ExpedienteClinicoRepository expedienteClinicoRepository,
        ExpedienteClinicoMapper expedienteClinicoMapper
    ) {
        this.expedienteClinicoRepository = expedienteClinicoRepository;
        this.expedienteClinicoMapper = expedienteClinicoMapper;
    }

    /**
     * Return a {@link Page} of {@link ExpedienteClinicoDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ExpedienteClinicoDTO> findByCriteria(ExpedienteClinicoCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ExpedienteClinico> specification = createSpecification(criteria);
        return expedienteClinicoRepository.findAll(specification, page).map(expedienteClinicoMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ExpedienteClinicoCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<ExpedienteClinico> specification = createSpecification(criteria);
        return expedienteClinicoRepository.count(specification);
    }

    /**
     * Function to convert {@link ExpedienteClinicoCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ExpedienteClinico> createSpecification(ExpedienteClinicoCriteria criteria) {
        Specification<ExpedienteClinico> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), ExpedienteClinico_.id),
                buildStringSpecification(criteria.getNumeroExpediente(), ExpedienteClinico_.numeroExpediente),
                buildRangeSpecification(criteria.getFechaApertura(), ExpedienteClinico_.fechaApertura),
                buildStringSpecification(criteria.getObservaciones(), ExpedienteClinico_.observaciones),
                buildSpecification(criteria.getConsultaId(), root ->
                    root.join(ExpedienteClinico_.consultas, JoinType.LEFT).get(ConsultaMedica_.id)
                ),
                buildSpecification(criteria.getHistorialId(), root ->
                    root.join(ExpedienteClinico_.historials, JoinType.LEFT).get(HistorialClinico_.id)
                ),
                buildSpecification(criteria.getPacienteId(), root -> root.join(ExpedienteClinico_.paciente, JoinType.LEFT).get(Paciente_.id)
                )
            );
        }
        return specification;
    }
}
