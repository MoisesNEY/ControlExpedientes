package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.service.RoleAdministrationService;
import ni.edu.mney.service.dto.RoleDefinitionDTO;
import ni.edu.mney.service.dto.RoleManagementCatalogDTO;
import ni.edu.mney.service.dto.RoleUpsertDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_ROLES_MANAGE + "')")
public class AdminRoleResource {

    private final RoleAdministrationService roleAdministrationService;

    public AdminRoleResource(RoleAdministrationService roleAdministrationService) {
        this.roleAdministrationService = roleAdministrationService;
    }

    @GetMapping("")
    public List<RoleDefinitionDTO> getRoles() {
        return roleAdministrationService.getAllRoles();
    }

    @GetMapping("/catalog")
    public RoleManagementCatalogDTO getCatalog() {
        return roleAdministrationService.getCatalog();
    }

    @PostMapping("")
    public ResponseEntity<RoleDefinitionDTO> createRole(@Valid @RequestBody RoleUpsertDTO request) {
        return ResponseEntity.ok(roleAdministrationService.createRole(request));
    }

    @PutMapping("/{roleName}")
    public ResponseEntity<RoleDefinitionDTO> updateRole(@PathVariable String roleName, @Valid @RequestBody RoleUpsertDTO request) {
        return ResponseEntity.ok(roleAdministrationService.updateRole(roleName, request));
    }

    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleName) {
        roleAdministrationService.deleteRole(roleName);
        return ResponseEntity.noContent().build();
    }
}
