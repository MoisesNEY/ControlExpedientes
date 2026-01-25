package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.SignosVitales;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.dto.SignosVitalesDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SignosVitales} and its DTO {@link SignosVitalesDTO}.
 */
@Mapper(componentModel = "spring")
public interface SignosVitalesMapper extends EntityMapper<SignosVitalesDTO, SignosVitales> {
    @Mapping(target = "consulta", source = "consulta", qualifiedByName = "consultaMedicaId")
    SignosVitalesDTO toDto(SignosVitales s);

    @Named("consultaMedicaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConsultaMedicaDTO toDtoConsultaMedicaId(ConsultaMedica consultaMedica);
}
