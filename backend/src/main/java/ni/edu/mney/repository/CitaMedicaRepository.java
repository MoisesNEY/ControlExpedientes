package ni.edu.mney.repository;

import java.util.List;
import java.util.Optional;
import ni.edu.mney.domain.CitaMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CitaMedica entity.
 */
@Repository
public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long>, JpaSpecificationExecutor<CitaMedica> {
    @Query("select citaMedica from CitaMedica citaMedica where citaMedica.user.login = ?#{authentication.name}")
    List<CitaMedica> findByUserIsCurrentUser();

    default Optional<CitaMedica> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CitaMedica> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CitaMedica> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select citaMedica from CitaMedica citaMedica left join fetch citaMedica.user",
        countQuery = "select count(citaMedica) from CitaMedica citaMedica"
    )
    Page<CitaMedica> findAllWithToOneRelationships(Pageable pageable);

    @Query("select citaMedica from CitaMedica citaMedica left join fetch citaMedica.user")
    List<CitaMedica> findAllWithToOneRelationships();

    @Query("select citaMedica from CitaMedica citaMedica left join fetch citaMedica.user where citaMedica.id =:id")
    Optional<CitaMedica> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("select c from CitaMedica c left join fetch c.paciente p left join fetch p.expediente where c.id = :id")
    Optional<CitaMedica> findWithPacienteAndExpedienteById(@Param("id") Long id);
}
