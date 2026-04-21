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

    public boolean validateCurrentUserCredentials(String username, String password) {
        return SecurityUtils.getCurrentUserLogin()
            .filter(currentLogin -> currentLogin.equalsIgnoreCase(username == null ? "" : username.trim()))
            .map(currentLogin -> keycloakAdminService.validateUserCredentials(currentLogin, password))
            .orElse(false);
    }

    public boolean validateCurrentUserPassword(String password) {
        return SecurityUtils.getCurrentUserLogin().map(login -> validateCurrentUserCredentials(login, password)).orElse(false);
    }
}
