package ni.edu.mney.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "app_role_definition")
public class RoleDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull
    @Size(max = 50)
    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    @NotNull
    @Column(name = "system_role", nullable = false)
    private boolean systemRole;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_role_definition_permission", joinColumns = @JoinColumn(name = "role_name"))
    @Column(name = "permission_code", nullable = false, length = 100)
    private Set<String> permissions = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_role_definition_composite", joinColumns = @JoinColumn(name = "role_name"))
    @Column(name = "composite_role_name", nullable = false, length = 50)
    private Set<String> compositeRoles = new LinkedHashSet<>();

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions == null ? new LinkedHashSet<>() : new LinkedHashSet<>(permissions);
    }

    public Set<String> getCompositeRoles() {
        return compositeRoles;
    }

    public void setCompositeRoles(Set<String> compositeRoles) {
        this.compositeRoles = compositeRoles == null ? new LinkedHashSet<>() : new LinkedHashSet<>(compositeRoles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoleDefinition)) {
            return false;
        }
        RoleDefinition that = (RoleDefinition) o;
        return Objects.equals(roleName, that.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleName);
    }
}
