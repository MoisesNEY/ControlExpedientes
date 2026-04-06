package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.ResultadoLaboratorio;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.dto.PacienteDTO;
import ni.edu.mney.service.dto.ResultadoLaboratorioDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ResultadoLaboratorio} and its DTO {@link ResultadoLaboratorioDTO}.
 */
@Mapper(componentModel = "spring")
public interface ResultadoLaboratorioMapper extends EntityMapper<ResultadoLaboratorioDTO, ResultadoLaboratorio> {
    @Mapping(target = "paciente", source = "paciente", qualifiedByName = "pacienteId")
    @Mapping(target = "consulta", source = "consulta", qualifiedByName = "consultaMedicaId")
    ResultadoLaboratorioDTO toDto(ResultadoLaboratorio s);

    @Named("pacienteId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PacienteDTO toDtoPacienteId(Paciente paciente);

    @Named("consultaMedicaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConsultaMedicaDTO toDtoConsultaMedicaId(ConsultaMedica consultaMedica);
}
