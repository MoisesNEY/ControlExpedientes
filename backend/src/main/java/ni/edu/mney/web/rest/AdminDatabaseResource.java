package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.service.CredentialValidationService;
import ni.edu.mney.service.DatabaseBackupService;
import ni.edu.mney.service.dto.ActionConfirmationDTO;
import ni.edu.mney.service.dto.DatabaseBackupSettingsDTO;
import ni.edu.mney.service.dto.DatabaseBackupSettingsUpdateRequestDTO;
import ni.edu.mney.service.dto.DatabaseBackupSummaryDTO;
import ni.edu.mney.service.dto.StoredBackupRestoreRequestDTO;
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
import org.springframework.web.bind.annotation.RequestPart;
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

    @PostMapping("/export")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_EXPORT + "')")
    public ResponseEntity<byte[]> exportDatabase(@Valid @RequestBody ActionConfirmationDTO confirmation) {
        LOG.debug("REST request para exportar respaldo de base de datos");
        validateConfirmation(confirmation, "EXPORTAR");
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
    public ResponseEntity<DatabaseBackupSettingsDTO> updateSettings(@Valid @RequestBody DatabaseBackupSettingsUpdateRequestDTO request) {
        LOG.debug("REST request para actualizar configuración de respaldos automáticos");
        validateConfirmation(request.confirmation(), "PROGRAMAR");
        return ResponseEntity.ok(databaseBackupService.updateSettings(request.settings()));
    }

    @PostMapping(value = "/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_RESTORE + "')")
    public ResponseEntity<Void> restoreDatabase(
        @RequestPart("file") MultipartFile file,
        @Valid @RequestPart("confirmation") ActionConfirmationDTO confirmation
    ) {
        LOG.debug("REST request para restaurar respaldo de base de datos");
        validateConfirmation(confirmation, "RESTAURAR");
        databaseBackupService.restoreDatabase(file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore/stored")
    @PreAuthorize("@permissionSecurityService.hasPermission('" + AppPermissionCatalog.ADMIN_DATABASE_RESTORE + "')")
    public ResponseEntity<Void> restoreStoredBackup(@Valid @RequestBody StoredBackupRestoreRequestDTO request) {
        LOG.debug("REST request para restaurar respaldo almacenado {}", request.filename());
        validateConfirmation(request.confirmation(), "RESTAURAR");
        databaseBackupService.restoreStoredBackup(request.filename());
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

    private void validateConfirmation(ActionConfirmationDTO confirmation, String expectedWord) {
        if (confirmation == null) {
            throw new IllegalArgumentException("Debe confirmar la operación con sus credenciales.");
        }
        if (!expectedWord.equalsIgnoreCase(confirmation.confirmationWord())) {
            throw new IllegalArgumentException("La palabra de confirmación no es válida para esta operación.");
        }
        if (!credentialValidationService.validateCurrentUserCredentials(confirmation.username(), confirmation.password())) {
            throw new IllegalArgumentException("Debe confirmar su usuario y contraseña actual para ejecutar la operación.");
        }
    }
}
