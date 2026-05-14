package ni.edu.mney.service.dto;

import java.util.Set;

public record RoleDefinitionDTO(
    String roleName,
    String description,
    boolean systemRole,
    Set<String> compositeRoles,
    Set<String> permissions
) {}
