package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.InteraccionMedicamentosa;
import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.service.dto.InteraccionMedicamentosaDTO;
import ni.edu.mney.service.dto.MedicamentoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link InteraccionMedicamentosa} and its DTO {@link InteraccionMedicamentosaDTO}.
 */
@Mapper(componentModel = "spring")
public interface InteraccionMedicamentosaMapper extends EntityMapper<InteraccionMedicamentosaDTO, InteraccionMedicamentosa> {
    @Mapping(target = "medicamentoA", source = "medicamentoA", qualifiedByName = "medicamentoId")
    @Mapping(target = "medicamentoB", source = "medicamentoB", qualifiedByName = "medicamentoId")
    InteraccionMedicamentosaDTO toDto(InteraccionMedicamentosa s);

    @Named("medicamentoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    MedicamentoDTO toDtoMedicamentoId(Medicamento medicamento);
}
