package ni.edu.mney.repository;

import ni.edu.mney.domain.ExpedienteClinico;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ExpedienteClinico entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ExpedienteClinicoRepository
        extends JpaRepository<ExpedienteClinico, Long>, JpaSpecificationExecutor<ExpedienteClinico> {
    java.util.Optional<ExpedienteClinico> findTopByOrderByIdDesc();

    java.util.Optional<ExpedienteClinico> findByPacienteId(Long pacienteId);

    @EntityGraph(attributePaths = { "consultas", "consultas.signosVitales", "consultas.diagnosticos",
            "consultas.recetas", "consultas.recetas.medicamento" })
    @Query("SELECT e FROM ExpedienteClinico e WHERE e.id = :id")
    java.util.Optional<ExpedienteClinico> findOneWithTimelineData(
            @org.springframework.data.repository.query.Param("id") Long id);
}
