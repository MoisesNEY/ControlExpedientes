package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.SystemSettingsService;
import ni.edu.mney.service.dto.SystemSettingsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/settings")
public class SystemSettingsResource {

    private final SystemSettingsService systemSettingsService;

    public SystemSettingsResource(SystemSettingsService systemSettingsService) {
        this.systemSettingsService = systemSettingsService;
    }

    @GetMapping
    public ResponseEntity<SystemSettingsDTO> getSettings() {
        return ResponseEntity.ok(systemSettingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<SystemSettingsDTO> updateSettings(@Valid @RequestBody SystemSettingsDTO request) {
        return ResponseEntity.ok(systemSettingsService.updateSettings(request));
    }
}
