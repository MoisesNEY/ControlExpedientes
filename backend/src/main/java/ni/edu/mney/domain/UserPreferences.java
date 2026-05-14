package ni.edu.mney.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "app_user_preferences")
public class UserPreferences extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Size(max = 50)
    @Column(name = "user_login", nullable = false, length = 50)
    private String userLogin;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Column(name = "sidebar_color", length = 7)
    private String sidebarColor;

    @Pattern(regexp = "^(light|dark)$")
    @Column(name = "theme", length = 10)
    private String theme;

    @Column(name = "device_backup_downloads_enabled")
    private Boolean deviceBackupDownloadsEnabled;

    @Override
    public String getId() {
        return userLogin;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Boolean getDeviceBackupDownloadsEnabled() {
        return deviceBackupDownloadsEnabled;
    }

    public void setDeviceBackupDownloadsEnabled(Boolean deviceBackupDownloadsEnabled) {
        this.deviceBackupDownloadsEnabled = deviceBackupDownloadsEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserPreferences)) {
            return false;
        }
        UserPreferences that = (UserPreferences) o;
        return Objects.equals(userLogin, that.userLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userLogin);
    }
}
