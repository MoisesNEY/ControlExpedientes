package ni.edu.mney.repository;

import ni.edu.mney.domain.Diagnostico;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Diagnostico entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long>, JpaSpecificationExecutor<Diagnostico> {}
