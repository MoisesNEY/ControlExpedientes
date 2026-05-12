package ni.edu.mney.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountProfileUpdateDTO(
    @Size(max = 50) String firstName,
    @Size(max = 50) String lastName,
    @NotBlank @Email @Size(min = 5, max = 254) String email
) {}
