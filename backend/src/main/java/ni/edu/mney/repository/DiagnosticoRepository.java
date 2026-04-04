package ni.edu.mney.repository;

import java.time.LocalDate;
import java.util.List;
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
    @Query("SELECT d FROM Diagnostico d WHERE d.codigoCIE LIKE %:q% OR d.descripcion LIKE %:q%")
    Page<Diagnostico> search(@Param("q") String q, Pageable pageable);

    List<Diagnostico> findAllByConsultaFechaConsultaBetweenAndConsultaUserLogin(LocalDate start, LocalDate end, String login);
}
