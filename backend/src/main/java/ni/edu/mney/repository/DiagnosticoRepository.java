package ni.edu.mney.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import ni.edu.mney.domain.Diagnostico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Diagnostico entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long>, JpaSpecificationExecutor<Diagnostico> {
    @Query("""
        SELECT d
        FROM Diagnostico d
        WHERE d.consulta IS NULL
          AND (
            LOWER(COALESCE(d.codigoCIE, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(d.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY d.descripcion ASC
        """)
    Page<Diagnostico> search(@Param("q") String q, Pageable pageable);

    Optional<Diagnostico> findFirstByConsultaIsNullAndCodigoCIEIgnoreCase(String codigoCIE);

    Optional<Diagnostico> findFirstByConsultaIsNullAndDescripcionIgnoreCase(String descripcion);

    List<Diagnostico> findAllByConsultaFechaConsultaBetweenAndConsultaUserLogin(LocalDate start, LocalDate end, String login);
}
