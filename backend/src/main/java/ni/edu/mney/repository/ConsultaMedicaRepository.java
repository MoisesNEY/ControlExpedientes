package ni.edu.mney.repository;

import java.util.List;
import java.util.Optional;
import ni.edu.mney.domain.ConsultaMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ConsultaMedica entity.
 */
@Repository
public interface ConsultaMedicaRepository extends JpaRepository<ConsultaMedica, Long>, JpaSpecificationExecutor<ConsultaMedica> {
    @Query("select consultaMedica from ConsultaMedica consultaMedica where consultaMedica.user.login = ?#{authentication.name}")
    List<ConsultaMedica> findByUserIsCurrentUser();

    default Optional<ConsultaMedica> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ConsultaMedica> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ConsultaMedica> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select consultaMedica from ConsultaMedica consultaMedica left join fetch consultaMedica.user",
        countQuery = "select count(consultaMedica) from ConsultaMedica consultaMedica"
    )
    Page<ConsultaMedica> findAllWithToOneRelationships(Pageable pageable);

    @Query("select consultaMedica from ConsultaMedica consultaMedica left join fetch consultaMedica.user")
    List<ConsultaMedica> findAllWithToOneRelationships();

    @Query("select consultaMedica from ConsultaMedica consultaMedica left join fetch consultaMedica.user where consultaMedica.id =:id")
    Optional<ConsultaMedica> findOneWithToOneRelationships(@Param("id") Long id);
}
