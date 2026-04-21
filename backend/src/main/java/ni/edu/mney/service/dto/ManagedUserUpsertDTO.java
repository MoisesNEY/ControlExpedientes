package ni.edu.mney.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ManagedUserUpsertDTO(
    @NotBlank @Size(max = 50) String login,
    @Size(max = 50) String firstName,
    @Size(max = 50) String lastName,
    @Email @Size(max = 254) String email,
    boolean activated,
    List<String> roles,
    @Size(max = 255) String password,
    boolean temporaryPassword,
    List<String> requiredActions
) {}
