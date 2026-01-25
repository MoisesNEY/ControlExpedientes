package ni.edu.mney.repository;

import ni.edu.mney.domain.Receta;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Receta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long>, JpaSpecificationExecutor<Receta> {}
