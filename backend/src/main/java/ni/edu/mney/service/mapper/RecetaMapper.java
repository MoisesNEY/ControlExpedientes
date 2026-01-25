package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.domain.Receta;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.dto.MedicamentoDTO;
import ni.edu.mney.service.dto.RecetaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Receta} and its DTO {@link RecetaDTO}.
 */
@Mapper(componentModel = "spring")
public interface RecetaMapper extends EntityMapper<RecetaDTO, Receta> {
    @Mapping(target = "medicamento", source = "medicamento", qualifiedByName = "medicamentoId")
    @Mapping(target = "consulta", source = "consulta", qualifiedByName = "consultaMedicaId")
    RecetaDTO toDto(Receta s);

    @Named("medicamentoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MedicamentoDTO toDtoMedicamentoId(Medicamento medicamento);

    @Named("consultaMedicaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConsultaMedicaDTO toDtoConsultaMedicaId(ConsultaMedica consultaMedica);
}
