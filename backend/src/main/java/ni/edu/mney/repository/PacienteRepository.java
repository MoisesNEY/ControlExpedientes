package ni.edu.mney.repository;

import java.util.Optional;
import ni.edu.mney.domain.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Paciente entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long>, JpaSpecificationExecutor<Paciente> {
    @Query("SELECT p FROM Paciente p WHERE p.expediente.id = :expedienteId")
    Optional<Paciente> findByExpedienteId(@Param("expedienteId") Long expedienteId);

    @EntityGraph(attributePaths = { "expediente" })
    Page<Paciente> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "expediente" })
    @Query("select paciente from Paciente paciente where paciente.id = :id")
    Optional<Paciente> findOneWithEagerRelationships(@Param("id") Long id);

    /**
     * Verifica si existe un paciente con el código dado (para garantizar unicidad
     * en autogeneración)
     */
    boolean existsByCodigo(String codigo);
}
