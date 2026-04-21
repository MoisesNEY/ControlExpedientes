package ni.edu.mney.web.rest;

import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.service.CredentialValidationService;
import ni.edu.mney.service.DatabaseBackupService;
import ni.edu.mney.service.dto.DatabaseBackupSettingsDTO;
import ni.edu.mney.service.dto.DatabaseBackupSummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/database")
public class AdminDatabaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdminDatabaseResource.class);

    private final DatabaseBackupService databaseBackupService;
    private final CredentialValidationService credentialValidationService;

    public AdminDatabaseResource(DatabaseBackupService databaseBackupService, CredentialValidationService credentialValidationService) {
        this.databaseBackupService = databaseBackupService;
        this.credentialValidationService = credentialValidationService;
    }

    @GetMapping("/export")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_EXPORT + "')")
    public ResponseEntity<byte[]> exportDatabase() {
        LOG.debug("REST request para exportar respaldo de base de datos");
        DatabaseBackupService.BackupFile backup = databaseBackupService.exportDatabase();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(backup.contentType()));
        headers.setContentDispositionFormData("attachment", backup.filename());

        return ResponseEntity.ok().headers(headers).body(backup.content());
    }

    @GetMapping("/summary")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_VIEW + "')")
    public ResponseEntity<DatabaseBackupSummaryDTO> getSummary() {
        LOG.debug("REST request para consultar configuración e historial de respaldos");
        return ResponseEntity.ok(databaseBackupService.getSummary());
    }

    @PutMapping("/settings")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_VIEW + "')")
    public ResponseEntity<DatabaseBackupSettingsDTO> updateSettings(@RequestBody DatabaseBackupSettingsDTO settings) {
        LOG.debug("REST request para actualizar configuración de respaldos automáticos");
        return ResponseEntity.ok(databaseBackupService.updateSettings(settings));
    }

    @PostMapping(value = "/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_RESTORE + "')")
    public ResponseEntity<Void> restoreDatabase(@RequestParam("file") MultipartFile file, @RequestParam("password") String password) {
        LOG.debug("REST request para restaurar respaldo de base de datos");
        validatePassword(password);
        databaseBackupService.restoreDatabase(file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore/stored")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_RESTORE + "')")
    public ResponseEntity<Void> restoreStoredBackup(@RequestParam("filename") String filename, @RequestParam("password") String password) {
        LOG.debug("REST request para restaurar respaldo almacenado {}", filename);
        validatePassword(password);
        databaseBackupService.restoreStoredBackup(filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stored")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_EXPORT + "')")
    public ResponseEntity<byte[]> downloadStoredBackup(@RequestParam("filename") String filename) {
        LOG.debug("REST request para descargar respaldo almacenado {}", filename);
        DatabaseBackupService.BackupFile backup = databaseBackupService.downloadStoredBackup(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(backup.contentType()));
        headers.setContentDispositionFormData("attachment", backup.filename());

        return ResponseEntity.ok().headers(headers).body(backup.content());
    }

    private void validatePassword(String password) {
        if (!credentialValidationService.validateCurrentUserPassword(password)) {
            throw new IllegalArgumentException("Debe confirmar su contraseña actual para restaurar la base de datos.");
        }
    }
}
