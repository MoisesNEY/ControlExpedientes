package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.service.AdminSecurityExportService;
import ni.edu.mney.service.RoleAdministrationService;
import ni.edu.mney.service.dto.RoleDefinitionDTO;
import ni.edu.mney.service.dto.RoleManagementCatalogDTO;
import ni.edu.mney.service.dto.RoleUpsertDTO;
import org.springframework.http.HttpHeaders;
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
public class AdminRoleResource {

    private final RoleAdministrationService roleAdministrationService;
    private final AdminSecurityExportService adminSecurityExportService;

    public AdminRoleResource(RoleAdministrationService roleAdministrationService, AdminSecurityExportService adminSecurityExportService) {
        this.roleAdministrationService = roleAdministrationService;
        this.adminSecurityExportService = adminSecurityExportService;
    }

    @GetMapping("")
    @PreAuthorize(
        "@permissionSecurityService.hasPermission('" +
        AppPermissionCatalog.ADMIN_ROLES_VIEW +
        "') or @permissionSecurityService.hasPermission('" +
        AppPermissionCatalog.ADMIN_ROLES_MANAGE +
        "') or @permissionSecurityService.hasPermission('" +
        AppPermissionCatalog.ADMIN_ROLES_EXPORT +
        "')"
    )
    public List<RoleDefinitionDTO> getRoles() {
        return roleAdministrationService.getAllRoles();
    }

    @GetMapping("/catalog")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_ROLES_MANAGE + "')")
    public RoleManagementCatalogDTO getCatalog() {
        return roleAdministrationService.getCatalog();
    }

    @GetMapping("/export")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_ROLES_EXPORT + "')")
    public ResponseEntity<byte[]> exportRoles() {
        AdminSecurityExportService.ExportedSpreadsheet export = adminSecurityExportService.exportRoles(roleAdministrationService.getAllRoles());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + export.filename() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, export.contentType());
        return ResponseEntity.ok().headers(headers).body(export.content());
    }

    @PostMapping("")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_ROLES_MANAGE + "')")
    public ResponseEntity<RoleDefinitionDTO> createRole(@Valid @RequestBody RoleUpsertDTO request) {
        return ResponseEntity.ok(roleAdministrationService.createRole(request));
    }

    @PutMapping("/{roleName}")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_ROLES_MANAGE + "')")
    public ResponseEntity<RoleDefinitionDTO> updateRole(@PathVariable String roleName, @Valid @RequestBody RoleUpsertDTO request) {
        return ResponseEntity.ok(roleAdministrationService.updateRole(roleName, request));
    }

    @DeleteMapping("/{roleName}")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_ROLES_MANAGE + "')")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleName) {
        roleAdministrationService.deleteRole(roleName);
        return ResponseEntity.noContent().build();
    }
}
