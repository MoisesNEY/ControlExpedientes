package ni.edu.mney.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "app_system_settings")
public class SystemSettings extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final long SINGLETON_ID = 1L;
    public static final String DEFAULT_PRIMARY_COLOR = "#0284c7";
    public static final String DEFAULT_SIDEBAR_COLOR = "#071e2b";
    public static final String DEFAULT_APPLICATION_NAME = "Control Expedientes";
    public static final String DEFAULT_BRAND_NAME = "ClinData";
    public static final String DEFAULT_THEME = "light";

    @Id
    @Column(name = "id")
    private Long id = SINGLETON_ID;

    @NotNull
    @Size(max = 80)
    @Column(name = "application_name", nullable = false, length = 80)
    private String applicationName = DEFAULT_APPLICATION_NAME;

    @NotNull
    @Size(max = 80)
    @Column(name = "brand_name", nullable = false, length = 80)
    private String brandName = DEFAULT_BRAND_NAME;

    @NotNull
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Column(name = "primary_color", nullable = false, length = 7)
    private String primaryColor = DEFAULT_PRIMARY_COLOR;

    @NotNull
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Column(name = "sidebar_color", nullable = false, length = 7)
    private String sidebarColor = DEFAULT_SIDEBAR_COLOR;

    @NotNull
    @Pattern(regexp = "^(light|dark)$")
    @Column(name = "default_theme", nullable = false, length = 10)
    private String defaultTheme = DEFAULT_THEME;

    @NotNull
    @Column(name = "default_device_backup_downloads_enabled", nullable = false)
    private boolean defaultDeviceBackupDownloadsEnabled = true;

    @NotNull
    @Column(name = "show_backup_download_reminders", nullable = false)
    private boolean showBackupDownloadReminders = true;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSidebarColor() {
        return sidebarColor;
    }

    public void setSidebarColor(String sidebarColor) {
        this.sidebarColor = sidebarColor;
    }

    public String getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(String defaultTheme) {
        this.defaultTheme = defaultTheme;
    }

    public boolean isDefaultDeviceBackupDownloadsEnabled() {
        return defaultDeviceBackupDownloadsEnabled;
    }

    public void setDefaultDeviceBackupDownloadsEnabled(boolean defaultDeviceBackupDownloadsEnabled) {
        this.defaultDeviceBackupDownloadsEnabled = defaultDeviceBackupDownloadsEnabled;
    }

    public boolean isShowBackupDownloadReminders() {
        return showBackupDownloadReminders;
    }

    public void setShowBackupDownloadReminders(boolean showBackupDownloadReminders) {
        this.showBackupDownloadReminders = showBackupDownloadReminders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemSettings)) {
            return false;
        }
        SystemSettings that = (SystemSettings) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
