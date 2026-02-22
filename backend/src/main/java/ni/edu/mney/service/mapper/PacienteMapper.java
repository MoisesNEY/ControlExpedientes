package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.dto.PacienteDTO;
import ni.edu.mney.service.dto.PacientePublicDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Paciente} and its DTO {@link PacienteDTO}.
 */
@Mapper(componentModel = "spring")
public interface PacienteMapper extends EntityMapper<PacienteDTO, Paciente> {
    @Mapping(target = "expediente", source = "expediente", qualifiedByName = "expedienteClinicoId")
    PacienteDTO toDto(Paciente s);

    PacientePublicDTO toPublicDto(Paciente s);

    @Named("expedienteClinicoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ExpedienteClinicoDTO toDtoExpedienteClinicoId(ExpedienteClinico expedienteClinico);
}
