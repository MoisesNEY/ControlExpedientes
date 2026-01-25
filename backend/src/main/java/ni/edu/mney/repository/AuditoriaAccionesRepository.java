package ni.edu.mney.repository;

import java.util.List;
import java.util.Optional;
import ni.edu.mney.domain.AuditoriaAcciones;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AuditoriaAcciones entity.
 */
@Repository
public interface AuditoriaAccionesRepository extends JpaRepository<AuditoriaAcciones, Long>, JpaSpecificationExecutor<AuditoriaAcciones> {
    @Query("select auditoriaAcciones from AuditoriaAcciones auditoriaAcciones where auditoriaAcciones.user.login = ?#{authentication.name}")
    List<AuditoriaAcciones> findByUserIsCurrentUser();

    default Optional<AuditoriaAcciones> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AuditoriaAcciones> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AuditoriaAcciones> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select auditoriaAcciones from AuditoriaAcciones auditoriaAcciones left join fetch auditoriaAcciones.user",
        countQuery = "select count(auditoriaAcciones) from AuditoriaAcciones auditoriaAcciones"
    )
    Page<AuditoriaAcciones> findAllWithToOneRelationships(Pageable pageable);

    @Query("select auditoriaAcciones from AuditoriaAcciones auditoriaAcciones left join fetch auditoriaAcciones.user")
    List<AuditoriaAcciones> findAllWithToOneRelationships();

    @Query(
        "select auditoriaAcciones from AuditoriaAcciones auditoriaAcciones left join fetch auditoriaAcciones.user where auditoriaAcciones.id =:id"
    )
    Optional<AuditoriaAcciones> findOneWithToOneRelationships(@Param("id") Long id);
}
