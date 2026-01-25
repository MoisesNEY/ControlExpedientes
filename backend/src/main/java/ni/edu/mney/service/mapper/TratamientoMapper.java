package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Tratamiento;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.dto.TratamientoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Tratamiento} and its DTO {@link TratamientoDTO}.
 */
@Mapper(componentModel = "spring")
public interface TratamientoMapper extends EntityMapper<TratamientoDTO, Tratamiento> {
    @Mapping(target = "consulta", source = "consulta", qualifiedByName = "consultaMedicaId")
    TratamientoDTO toDto(Tratamiento s);

    @Named("consultaMedicaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConsultaMedicaDTO toDtoConsultaMedicaId(ConsultaMedica consultaMedica);
}
