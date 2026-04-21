package ni.edu.mney.web.rest;

import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.DatabaseBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/database")
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public class AdminDatabaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdminDatabaseResource.class);

    private final DatabaseBackupService databaseBackupService;

    public AdminDatabaseResource(DatabaseBackupService databaseBackupService) {
        this.databaseBackupService = databaseBackupService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDatabase() {
        LOG.debug("REST request para exportar respaldo de base de datos");
        DatabaseBackupService.BackupFile backup = databaseBackupService.exportDatabase();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(backup.contentType()));
        headers.setContentDispositionFormData("attachment", backup.filename());

        return ResponseEntity.ok().headers(headers).body(backup.content());
    }

    @PostMapping(value = "/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> restoreDatabase(@RequestParam("file") MultipartFile file) {
        LOG.debug("REST request para restaurar respaldo de base de datos");
        databaseBackupService.restoreDatabase(file);
        return ResponseEntity.ok().build();
    }
}
