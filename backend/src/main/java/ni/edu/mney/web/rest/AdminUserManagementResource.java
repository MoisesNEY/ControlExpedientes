package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.service.AdminSecurityExportService;
import ni.edu.mney.service.UserAdministrationService;
import ni.edu.mney.service.dto.ManagedUserDTO;
import ni.edu.mney.service.dto.ManagedUserUpsertDTO;
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
@RequestMapping("/api/admin/users")
@PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_USERS_MANAGE + "')")
public class AdminUserManagementResource {

    private final UserAdministrationService userAdministrationService;
    private final AdminSecurityExportService adminSecurityExportService;

    public AdminUserManagementResource(
        UserAdministrationService userAdministrationService,
        AdminSecurityExportService adminSecurityExportService
    ) {
        this.userAdministrationService = userAdministrationService;
        this.adminSecurityExportService = adminSecurityExportService;
    }

    @GetMapping("")
    public List<ManagedUserDTO> getUsers() {
        return userAdministrationService.getAllUsers();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers() {
        AdminSecurityExportService.ExportedSpreadsheet export = adminSecurityExportService.exportUsers(userAdministrationService.getAllUsers());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + export.filename() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, export.contentType());
        return ResponseEntity.ok().headers(headers).body(export.content());
    }

    @PostMapping("")
    public ResponseEntity<ManagedUserDTO> createUser(@Valid @RequestBody ManagedUserUpsertDTO request) {
        return ResponseEntity.ok(userAdministrationService.createUser(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ManagedUserDTO> updateUser(@PathVariable String userId, @Valid @RequestBody ManagedUserUpsertDTO request) {
        return ResponseEntity.ok(userAdministrationService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userAdministrationService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
