package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import ni.edu.mney.service.UserPreferencesService;
import ni.edu.mney.service.dto.UserPreferencesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account/preferences")
public class UserPreferencesResource {

    private final UserPreferencesService userPreferencesService;

    public UserPreferencesResource(UserPreferencesService userPreferencesService) {
        this.userPreferencesService = userPreferencesService;
    }

    @GetMapping
    public ResponseEntity<UserPreferencesDTO> getCurrentUserPreferences() {
        return ResponseEntity.ok(userPreferencesService.getCurrentUserPreferences());
    }

    @PutMapping
    public ResponseEntity<UserPreferencesDTO> updateCurrentUserPreferences(@Valid @RequestBody UserPreferencesDTO request) {
        return ResponseEntity.ok(userPreferencesService.updateCurrentUserPreferences(request));
    }
}
