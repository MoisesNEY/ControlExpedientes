package ni.edu.mney.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DatabaseBackupSettingsUpdateRequestDTO(
    @NotNull @Valid DatabaseBackupSettingsDTO settings,
    @NotNull @Valid ActionConfirmationDTO confirmation
) {}
