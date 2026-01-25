package ni.edu.mney.repository;

import ni.edu.mney.domain.Tratamiento;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Tratamiento entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TratamientoRepository extends JpaRepository<Tratamiento, Long>, JpaSpecificationExecutor<Tratamiento> {}
