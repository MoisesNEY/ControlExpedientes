package ni.edu.mney.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SystemSettingsDTO(
    @NotNull @Size(max = 80) String applicationName,
    @NotNull @Size(max = 80) String brandName,
    @NotNull @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String primaryColor,
    @NotNull @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String sidebarColor,
    @NotNull @Pattern(regexp = "^(light|dark)$") String defaultTheme,
    boolean defaultDeviceBackupDownloadsEnabled,
    boolean showBackupDownloadReminders
) {}
