package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.dto.PacienteDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ExpedienteClinico} and its DTO {@link ExpedienteClinicoDTO}.
 */
@Mapper(componentModel = "spring")
public interface ExpedienteClinicoMapper extends EntityMapper<ExpedienteClinicoDTO, ExpedienteClinico> {
    @Mapping(target = "paciente", source = "paciente", qualifiedByName = "pacienteSummary")
    ExpedienteClinicoDTO toDto(ExpedienteClinico s);

    @Mapping(target = "paciente", source = "paciente", qualifiedByName = "pacienteFromId")
    @Mapping(target = "consultas", ignore = true)
    @Mapping(target = "historials", ignore = true)
    ExpedienteClinico toEntity(ExpedienteClinicoDTO dto);

    @Named("pacienteSummary")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "nombres", source = "nombres")
    @Mapping(target = "apellidos", source = "apellidos")
    PacienteDTO toDtoPacienteSummary(Paciente paciente);

    @Named("pacienteFromId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Paciente toEntityPacienteFromId(PacienteDTO paciente);
}
