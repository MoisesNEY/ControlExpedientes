package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.User;
import ni.edu.mney.service.dto.CitaMedicaDTO;
import ni.edu.mney.service.dto.PacienteDTO;
import ni.edu.mney.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CitaMedica} and its DTO {@link CitaMedicaDTO}.
 */
@Mapper(componentModel = "spring")
public interface CitaMedicaMapper extends EntityMapper<CitaMedicaDTO, CitaMedica> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "paciente", source = "paciente", qualifiedByName = "pacienteSummary")
    CitaMedicaDTO toDto(CitaMedica s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("pacienteSummary")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "nombres", source = "nombres")
    @Mapping(target = "apellidos", source = "apellidos")
    PacienteDTO toDtoPacienteSummary(Paciente paciente);
}
