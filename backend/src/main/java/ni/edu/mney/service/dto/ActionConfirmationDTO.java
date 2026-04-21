package ni.edu.mney.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActionConfirmationDTO(
    @NotBlank @Size(max = 100) String username,
    @NotBlank @Size(max = 255) String password,
    @NotBlank @Size(max = 30) String confirmationWord
) {}
