package ni.edu.mney.service.dto;

import jakarta.validation.constraints.Pattern;

public record UserPreferencesDTO(
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String primaryColor,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String sidebarColor,
    @Pattern(regexp = "^(light|dark)$") String theme,
    Boolean deviceBackupDownloadsEnabled
) {}
