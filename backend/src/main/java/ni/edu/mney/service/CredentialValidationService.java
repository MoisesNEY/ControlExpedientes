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
        return isCurrentUser(username) && validateCurrentUserPassword(password);
    }

    public boolean validateCurrentUserPassword(String password) {
        return SecurityUtils.getCurrentUserLogin().map(login -> keycloakAdminService.validateUserCredentials(login, password)).orElse(false);
    }

    public boolean isCurrentUser(String username) {
        return SecurityUtils.getCurrentUserLogin()
            .filter(currentLogin -> currentLogin.equalsIgnoreCase(username == null ? "" : username.trim()))
            .isPresent();
    }
}
