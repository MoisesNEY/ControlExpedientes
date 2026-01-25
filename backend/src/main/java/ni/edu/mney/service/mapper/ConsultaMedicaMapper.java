package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.User;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ConsultaMedica} and its DTO {@link ConsultaMedicaDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConsultaMedicaMapper extends EntityMapper<ConsultaMedicaDTO, ConsultaMedica> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "expediente", source = "expediente", qualifiedByName = "expedienteClinicoId")
    ConsultaMedicaDTO toDto(ConsultaMedica s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("expedienteClinicoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ExpedienteClinicoDTO toDtoExpedienteClinicoId(ExpedienteClinico expedienteClinico);
}
