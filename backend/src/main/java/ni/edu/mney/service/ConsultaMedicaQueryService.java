package ni.edu.mney.service;

import jakarta.persistence.criteria.JoinType;
import ni.edu.mney.domain.*; // for static metamodels
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.service.criteria.ConsultaMedicaCriteria;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.mapper.ConsultaMedicaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link ConsultaMedica} entities in the database.
 * The main input is a {@link ConsultaMedicaCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ConsultaMedicaDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ConsultaMedicaQueryService extends QueryService<ConsultaMedica> {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultaMedicaQueryService.class);

    private final ConsultaMedicaRepository consultaMedicaRepository;

    private final ConsultaMedicaMapper consultaMedicaMapper;

    public ConsultaMedicaQueryService(ConsultaMedicaRepository consultaMedicaRepository, ConsultaMedicaMapper consultaMedicaMapper) {
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.consultaMedicaMapper = consultaMedicaMapper;
    }

    /**
     * Return a {@link Page} of {@link ConsultaMedicaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ConsultaMedicaDTO> findByCriteria(ConsultaMedicaCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ConsultaMedica> specification = createSpecification(criteria);
        return consultaMedicaRepository.findAll(specification, page).map(consultaMedicaMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ConsultaMedicaCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<ConsultaMedica> specification = createSpecification(criteria);
        return consultaMedicaRepository.count(specification);
    }

    /**
     * Function to convert {@link ConsultaMedicaCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ConsultaMedica> createSpecification(ConsultaMedicaCriteria criteria) {
        Specification<ConsultaMedica> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), ConsultaMedica_.id),
                buildRangeSpecification(criteria.getFechaConsulta(), ConsultaMedica_.fechaConsulta),
                buildStringSpecification(criteria.getMotivoConsulta(), ConsultaMedica_.motivoConsulta),
                buildStringSpecification(criteria.getNotasMedicas(), ConsultaMedica_.notasMedicas),
                buildSpecification(criteria.getDiagnosticoId(), root ->
                    root.join(ConsultaMedica_.diagnosticos, JoinType.LEFT).get(Diagnostico_.id)
                ),
                buildSpecification(criteria.getTratamientoId(), root ->
                    root.join(ConsultaMedica_.tratamientos, JoinType.LEFT).get(Tratamiento_.id)
                ),
                buildSpecification(criteria.getSignosVitalesId(), root ->
                    root.join(ConsultaMedica_.signosVitales, JoinType.LEFT).get(SignosVitales_.id)
                ),
                buildSpecification(criteria.getRecetaId(), root -> root.join(ConsultaMedica_.recetas, JoinType.LEFT).get(Receta_.id)),
                buildSpecification(criteria.getUserId(), root -> root.join(ConsultaMedica_.user, JoinType.LEFT).get(User_.id)),
                buildSpecification(criteria.getExpedienteId(), root ->
                    root.join(ConsultaMedica_.expediente, JoinType.LEFT).get(ExpedienteClinico_.id)
                )
            );
        }
        return specification;
    }
}
