package ni.edu.mney.service.mapper;

import ni.edu.mney.domain.AuditoriaAcciones;
import ni.edu.mney.domain.User;
import ni.edu.mney.service.dto.AuditoriaAccionesDTO;
import ni.edu.mney.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AuditoriaAcciones} and its DTO {@link AuditoriaAccionesDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuditoriaAccionesMapper extends EntityMapper<AuditoriaAccionesDTO, AuditoriaAcciones> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    AuditoriaAccionesDTO toDto(AuditoriaAcciones s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
