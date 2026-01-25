package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.HistorialClinico;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.dto.HistorialClinicoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link HistorialClinico} and its DTO {@link HistorialClinicoDTO}.
 */
@Mapper(componentModel = "spring")
public interface HistorialClinicoMapper extends EntityMapper<HistorialClinicoDTO, HistorialClinico> {
    @Mapping(target = "expediente", source = "expediente", qualifiedByName = "expedienteClinicoId")
    HistorialClinicoDTO toDto(HistorialClinico s);

    @Named("expedienteClinicoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ExpedienteClinicoDTO toDtoExpedienteClinicoId(ExpedienteClinico expedienteClinico);
}
