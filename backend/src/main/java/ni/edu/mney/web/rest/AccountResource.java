package ni.edu.mney.web.rest;

import jakarta.validation.Valid;
import java.security.Principal;
import ni.edu.mney.security.KeycloakAdminService;
import ni.edu.mney.security.SecurityUtils;
import ni.edu.mney.service.CredentialValidationService;
import ni.edu.mney.service.UserService;
import ni.edu.mney.service.dto.AccountPasswordChangeDTO;
import ni.edu.mney.service.dto.AccountProfileUpdateDTO;
import ni.edu.mney.service.dto.AdminUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    private final UserService userService;

    private final CredentialValidationService credentialValidationService;

    private final KeycloakAdminService keycloakAdminService;

    public AccountResource(
        UserService userService,
        CredentialValidationService credentialValidationService,
        KeycloakAdminService keycloakAdminService
    ) {
        this.userService = userService;
        this.credentialValidationService = credentialValidationService;
        this.keycloakAdminService = keycloakAdminService;
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @param principal the current user; resolves to {@code null} if not authenticated.
     * @return the current user.
     * @throws AccountResourceException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    public AdminUserDTO getAccount(Principal principal) {
        if (principal instanceof AbstractAuthenticationToken) {
            return userService.getUserFromAuthentication((AbstractAuthenticationToken) principal);
        } else {
            throw new AccountResourceException("User could not be found");
        }
    }

    /**
     * {@code PUT  /account} : update the current user's visible profile.
     */
    @PutMapping("/account")
    public AdminUserDTO updateAccount(@Valid @RequestBody AccountProfileUpdateDTO request, Principal principal) {
        if (principal instanceof AbstractAuthenticationToken authToken) {
            return userService.updateCurrentUserProfile(request.firstName(), request.lastName(), request.email(), authToken);
        }
        throw new AccountResourceException("User could not be found");
    }

    /**
     * {@code POST  /account/change-password} : change the current user's password.
     */
    @PostMapping("/account/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody AccountPasswordChangeDTO request, Principal principal) {
        if (!(principal instanceof AbstractAuthenticationToken)) {
            throw new AccountResourceException("User could not be found");
        }
        if (!credentialValidationService.validateCurrentUserPassword(request.currentPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("User could not be found"));
        KeycloakAdminService.ManagedKeycloakUser currentUser = keycloakAdminService.getUserByUsername(login);
        keycloakAdminService.resetUserPasswordWithoutReload(currentUser.id(), request.newPassword(), false);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated.
     *
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)},
     * or with status {@code 401 (Unauthorized)} if not authenticated.
     */
    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }
}
