package ni.edu.mney.repository;

import java.util.List;
import ni.edu.mney.domain.InteraccionMedicamentosa;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InteraccionMedicamentosa entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InteraccionMedicamentosaRepository
    extends JpaRepository<InteraccionMedicamentosa, Long>, JpaSpecificationExecutor<InteraccionMedicamentosa> {

    List<InteraccionMedicamentosa> findByMedicamentoAIdOrMedicamentoBId(Long medAId, Long medBId);

    List<InteraccionMedicamentosa> findByMedicamentoAIdAndMedicamentoBIdOrMedicamentoAIdAndMedicamentoBId(
        Long a1, Long b1, Long a2, Long b2
    );

    @Query("SELECT i FROM InteraccionMedicamentosa i WHERE " +
           "(i.medicamentoA.id IN :medicamentoIds OR i.medicamentoB.id IN :medicamentoIds)")
    List<InteraccionMedicamentosa> findByMedicamentoIds(@Param("medicamentoIds") List<Long> medicamentoIds);

    @Query("SELECT i FROM InteraccionMedicamentosa i WHERE " +
           "i.medicamentoA.id IN :medicamentoIds AND i.medicamentoB.id IN :medicamentoIds")
    List<InteraccionMedicamentosa> findInteractionsBetweenMedicamentos(@Param("medicamentoIds") List<Long> medicamentoIds);
}
