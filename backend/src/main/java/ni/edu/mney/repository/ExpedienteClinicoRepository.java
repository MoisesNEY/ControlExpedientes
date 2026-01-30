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
}
