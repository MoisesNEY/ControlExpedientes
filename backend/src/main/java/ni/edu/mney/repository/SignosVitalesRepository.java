package ni.edu.mney.repository;

import ni.edu.mney.domain.SignosVitales;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SignosVitales entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SignosVitalesRepository
        extends JpaRepository<SignosVitales, Long>, JpaSpecificationExecutor<SignosVitales> {
    @Query("SELECT sv FROM SignosVitales sv JOIN sv.consulta c JOIN c.expediente.paciente p WHERE p.id = :pacienteId AND c.fechaConsulta = :fecha ORDER BY sv.id DESC")
    java.util.List<SignosVitales> findByPacienteIdAndFechaConsulta(
            @org.springframework.data.repository.query.Param("pacienteId") Long pacienteId,
            @org.springframework.data.repository.query.Param("fecha") java.time.LocalDate fecha);
}
