package ni.edu.mney.service.dto;

import java.util.List;

public record RoleManagementCatalogDTO(
    List<String> availableCompositeRoles,
    List<PermissionDefinitionDTO> permissions
) {}
