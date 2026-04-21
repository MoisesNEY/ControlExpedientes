package ni.edu.mney.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RoleUpsertDTO(
    @NotBlank @Pattern(regexp = "ROLE_[A-Z0-9_]+") @Size(max = 50) String roleName,
    @Size(max = 255) String description,
    Set<String> compositeRoles,
    Set<String> permissions
) {}
