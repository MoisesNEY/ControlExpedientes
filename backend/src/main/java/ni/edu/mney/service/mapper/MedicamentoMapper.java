package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.service.dto.MedicamentoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Medicamento} and its DTO {@link MedicamentoDTO}.
 */
@Mapper(componentModel = "spring")
public interface MedicamentoMapper extends EntityMapper<MedicamentoDTO, Medicamento> {}
