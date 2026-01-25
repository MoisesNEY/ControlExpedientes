package ni.edu.mney.repository;

import ni.edu.mney.domain.HistorialClinico;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the HistorialClinico entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long>, JpaSpecificationExecutor<HistorialClinico> {}
