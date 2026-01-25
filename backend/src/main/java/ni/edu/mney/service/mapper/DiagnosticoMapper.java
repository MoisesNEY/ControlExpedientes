package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Diagnostico;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.dto.DiagnosticoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Diagnostico} and its DTO {@link DiagnosticoDTO}.
 */
@Mapper(componentModel = "spring")
public interface DiagnosticoMapper extends EntityMapper<DiagnosticoDTO, Diagnostico> {
    @Mapping(target = "consulta", source = "consulta", qualifiedByName = "consultaMedicaId")
    DiagnosticoDTO toDto(Diagnostico s);

    @Named("consultaMedicaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConsultaMedicaDTO toDtoConsultaMedicaId(ConsultaMedica consultaMedica);
}
