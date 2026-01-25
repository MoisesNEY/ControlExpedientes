package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ExpedienteClinico} and its DTO {@link ExpedienteClinicoDTO}.
 */
@Mapper(componentModel = "spring")
public interface ExpedienteClinicoMapper extends EntityMapper<ExpedienteClinicoDTO, ExpedienteClinico> {}
