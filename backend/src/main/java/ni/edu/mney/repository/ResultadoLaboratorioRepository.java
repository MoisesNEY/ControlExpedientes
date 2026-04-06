package ni.edu.mney.repository;

import java.time.LocalDate;
import java.util.List;
import ni.edu.mney.domain.ResultadoLaboratorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ResultadoLaboratorio entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ResultadoLaboratorioRepository
        extends JpaRepository<ResultadoLaboratorio, Long>, JpaSpecificationExecutor<ResultadoLaboratorio> {

    Page<ResultadoLaboratorio> findByPacienteId(Long pacienteId, Pageable pageable);

    List<ResultadoLaboratorio> findByConsultaId(Long consultaId);

    Page<ResultadoLaboratorio> findByPacienteIdAndFechaExamenBetween(
            Long pacienteId, LocalDate from, LocalDate to, Pageable pageable);
}
