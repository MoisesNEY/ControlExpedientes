package ni.edu.mney.service;

import ni.edu.mney.security.KeycloakAdminService;
import ni.edu.mney.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class CredentialValidationService {

    private final KeycloakAdminService keycloakAdminService;

    public CredentialValidationService(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }

    public boolean validateCurrentUserPassword(String password) {
        return SecurityUtils.getCurrentUserLogin()
            .map(login -> keycloakAdminService.validateUserCredentials(login, password))
            .orElse(false);
    }
}
