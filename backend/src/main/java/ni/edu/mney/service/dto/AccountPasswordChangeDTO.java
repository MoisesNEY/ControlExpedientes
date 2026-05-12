package ni.edu.mney.service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public record AccountPasswordChangeDTO(
    @NotBlank @Size(max = 255) String currentPassword,
    @NotBlank @Size(min = 8, max = 255) String newPassword,
    @NotBlank @Size(max = 255) String confirmationPassword
) {
    @AssertTrue(message = "La confirmación de contraseña no coincide.")
    public boolean isPasswordConfirmationMatching() {
        return Objects.equals(newPassword, confirmationPassword);
    }
}
