package ni.edu.mney.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StoredBackupRestoreRequestDTO(
    @NotBlank @Size(max = 255) String filename,
    @NotNull @Valid ActionConfirmationDTO confirmation
) {}
