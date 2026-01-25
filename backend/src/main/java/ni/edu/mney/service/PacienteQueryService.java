package ni.edu.mney.service;

import jakarta.persistence.criteria.JoinType;
import ni.edu.mney.domain.*; // for static metamodels
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.repository.PacienteRepository;
import ni.edu.mney.service.criteria.PacienteCriteria;
import ni.edu.mney.service.dto.PacienteDTO;
import ni.edu.mney.service.mapper.PacienteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Paciente} entities in the database.
 * The main input is a {@link PacienteCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link PacienteDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PacienteQueryService extends QueryService<Paciente> {

    private static final Logger LOG = LoggerFactory.getLogger(PacienteQueryService.class);

    private final PacienteRepository pacienteRepository;

    private final PacienteMapper pacienteMapper;

    public PacienteQueryService(PacienteRepository pacienteRepository, PacienteMapper pacienteMapper) {
        this.pacienteRepository = pacienteRepository;
        this.pacienteMapper = pacienteMapper;
    }

    /**
     * Return a {@link Page} of {@link PacienteDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PacienteDTO> findByCriteria(PacienteCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Paciente> specification = createSpecification(criteria);
        return pacienteRepository.findAll(specification, page).map(pacienteMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PacienteCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Paciente> specification = createSpecification(criteria);
        return pacienteRepository.count(specification);
    }

    /**
     * Function to convert {@link PacienteCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Paciente> createSpecification(PacienteCriteria criteria) {
        Specification<Paciente> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Paciente_.id),
                buildStringSpecification(criteria.getCodigo(), Paciente_.codigo),
                buildStringSpecification(criteria.getNombres(), Paciente_.nombres),
                buildStringSpecification(criteria.getApellidos(), Paciente_.apellidos),
                buildSpecification(criteria.getSexo(), Paciente_.sexo),
                buildRangeSpecification(criteria.getFechaNacimiento(), Paciente_.fechaNacimiento),
                buildStringSpecification(criteria.getCedula(), Paciente_.cedula),
                buildStringSpecification(criteria.getTelefono(), Paciente_.telefono),
                buildStringSpecification(criteria.getDireccion(), Paciente_.direccion),
                buildSpecification(criteria.getEstadoCivil(), Paciente_.estadoCivil),
                buildStringSpecification(criteria.getEmail(), Paciente_.email),
                buildSpecification(criteria.getActivo(), Paciente_.activo),
                buildSpecification(criteria.getExpedienteId(), root ->
                    root.join(Paciente_.expediente, JoinType.LEFT).get(ExpedienteClinico_.id)
                ),
                buildSpecification(criteria.getCitaId(), root -> root.join(Paciente_.citas, JoinType.LEFT).get(CitaMedica_.id))
            );
        }
        return specification;
    }
}
