package ni.edu.mney.web.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import ni.edu.mney.security.PermissionAuthorityService;
import ni.edu.mney.security.KeycloakAdminService;
import ni.edu.mney.security.SecurityUtils;
import ni.edu.mney.service.UserService;
import ni.edu.mney.service.dto.AdminUserDTO;
import ni.edu.mney.web.rest.vm.LoginVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * REST controller for BFF session-based authentication.
 * <p>
 * Receives username/password from the frontend, authenticates against Keycloak
 * using the Direct Access Grant (Resource Owner Password Credentials), stores
 * the tokens in the server-side HttpSession, and returns an opaque session
 * cookie.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class AuthenticationResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);
    public static final String SESSION_ATTR_POST_LOGIN_REDIRECT = "AUTH_POST_LOGIN_REDIRECT";

    private static final String SESSION_ATTR_ACCESS_TOKEN = "KC_ACCESS_TOKEN";
    private static final String SESSION_ATTR_REFRESH_TOKEN = "KC_REFRESH_TOKEN";
    private static final String SESSION_ATTR_TOKEN_EXPIRY = "KC_TOKEN_EXPIRY";
    private static final String SESSION_ATTR_PENDING_CHALLENGE = "KC_PENDING_REQUIRED_ACTIONS";

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    private String clientSecret;

    @Value("${jhipster.cors.allowed-origins:}")
    private String allowedOrigins;

    private final JwtDecoder jwtDecoder;
    private final UserService userService;
    private final PermissionAuthorityService permissionAuthorityService;
    private final KeycloakAdminService keycloakAdminService;
    private final RestTemplate restTemplate = new RestTemplate();

    public AuthenticationResource(
        JwtDecoder jwtDecoder,
        UserService userService,
        PermissionAuthorityService permissionAuthorityService,
        KeycloakAdminService keycloakAdminService
    ) {
        this.jwtDecoder = jwtDecoder;
        this.userService = userService;
        this.permissionAuthorityService = permissionAuthorityService;
        this.keycloakAdminService = keycloakAdminService;
    }

    /**
     * {@code POST  /authenticate} : Authenticate a user via BFF pattern.
     * <p>
     * The frontend sends username + password. This endpoint authenticates
     * against Keycloak using Direct Access Grants, stores the tokens
     * in the server-side HttpSession, and sets a JSESSIONID cookie.
     * </p>
     *
     * @param loginVM the login view model with username and password.
     * @param request the HTTP request to create/access the session.
     * @return the user account information if authentication succeeds,
     *         or 401 if credentials are invalid.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginVM loginVM, HttpServletRequest request) {
        LOG.debug("BFF authenticate request for user: {}", loginVM.getUsername());

        try {
            // 1. Call Keycloak token endpoint with Direct Access Grant
            String tokenEndpoint = issuerUri + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", loginVM.getUsername());
            body.add("password", loginVM.getPassword());
            body.add("scope", "openid profile email offline_access");

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(tokenEndpoint, tokenRequest, Map.class);

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                LOG.warn("Keycloak returned null or empty token response for user: {}", loginVM.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas"));
            }

            AdminUserDTO userDTO = establishAuthenticatedSession(tokenResponse, request);

            LOG.info("BFF authentication successful for user: {}", loginVM.getUsername());
            return ResponseEntity.ok(userDTO);
        } catch (HttpClientErrorException e) {
            LOG.warn("Keycloak authentication failed for user {}: {}", loginVM.getUsername(), e.getStatusCode());
            boolean requiresBrowserLogin = requiresBrowserLogin(e);
            if (requiresBrowserLogin) {
                HttpSession session = request.getSession(true);
                try {
                    clearAuthenticatedSession(session);
                    KeycloakAdminService.ManagedKeycloakUser managedUser = keycloakAdminService.getUserByUsername(loginVM.getUsername());
                    PendingRequiredActionsChallenge challenge = new PendingRequiredActionsChallenge(
                        managedUser.id(),
                        managedUser.login(),
                        managedUser.firstName(),
                        managedUser.lastName(),
                        managedUser.email(),
                        managedUser.requiredActions(),
                        loginVM.getPassword()
                    );
                    session.setAttribute(SESSION_ATTR_PENDING_CHALLENGE, challenge);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(
                            Map.of(
                                "error",
                                "Acciones obligatorias pendientes",
                                "detail",
                                resolveAuthenticationDetail(e),
                                "requiresActionCompletion",
                                true,
                                "requiredActions",
                                challenge.requiredActions(),
                                "profile",
                                Map.of(
                                    "login",
                                    challenge.login(),
                                    "firstName",
                                    defaultString(challenge.firstName()),
                                    "lastName",
                                    defaultString(challenge.lastName()),
                                    "email",
                                    defaultString(challenge.email())
                                )
                            )
                        );
                } catch (RuntimeException challengeError) {
                    LOG.warn("No se pudo preparar el flujo interno de acciones obligatorias para {}", loginVM.getUsername(), challengeError);
                }
            }
            clearPendingChallenge(request.getSession(false));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                        Map.of(
                            "error",
                            "Credenciales inválidas",
                            "detail",
                            resolveAuthenticationDetail(e),
                            "requiresActionCompletion",
                            false
                        )
                    );
        } catch (Exception e) {
            LOG.error("Unexpected error during BFF authentication for user: {}", loginVM.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno de autenticación"));
        }
    }

    @PostMapping("/authenticate/required-actions")
    public ResponseEntity<?> completeRequiredActions(
        @Valid @RequestBody CompleteRequiredActionsVM request,
        HttpServletRequest httpRequest
    ) {
        HttpSession session = httpRequest.getSession(false);
        PendingRequiredActionsChallenge challenge = getPendingChallenge(session);
        try {
            KeycloakAdminService.ManagedKeycloakUser currentUser = keycloakAdminService.getUserByUsername(challenge.login());
            List<String> remainingActions = new ArrayList<>(currentUser.requiredActions());

            if (remainingActions.contains("UPDATE_PROFILE")) {
                validateProfileUpdate(request);
                currentUser = keycloakAdminService.updateUserProfile(
                    currentUser.id(),
                    request.firstName(),
                    request.lastName(),
                    request.email()
                );
                currentUser = keycloakAdminService.clearRequiredActions(currentUser.id(), List.of("UPDATE_PROFILE"));
                remainingActions = new ArrayList<>(currentUser.requiredActions());
            }

            if (remainingActions.contains("VERIFY_EMAIL")) {
                String effectiveEmail = firstNonBlank(request.email(), currentUser.email());
                if (effectiveEmail == null) {
                    throw new IllegalArgumentException("Debes indicar un correo válido antes de confirmar la verificación.");
                }
                if (!effectiveEmail.equals(currentUser.email())) {
                    currentUser = keycloakAdminService.updateUserProfile(currentUser.id(), currentUser.firstName(), currentUser.lastName(), effectiveEmail);
                }
                currentUser = keycloakAdminService.markEmailAsVerified(currentUser.id());
                currentUser = keycloakAdminService.clearRequiredActions(currentUser.id(), List.of("VERIFY_EMAIL"));
                remainingActions = new ArrayList<>(currentUser.requiredActions());
            }

            if (remainingActions.contains("UPDATE_PASSWORD")) {
                if (request.newPassword() == null || request.newPassword().isBlank()) {
                    throw new IllegalArgumentException("Debes definir una nueva contraseña para completar el acceso.");
                }
                keycloakAdminService.resetUserPassword(currentUser.id(), request.newPassword(), false);
                currentUser = keycloakAdminService.clearRequiredActions(currentUser.id(), List.of("UPDATE_PASSWORD"));
                remainingActions = new ArrayList<>(currentUser.requiredActions());
            }

            if (remainingActions.contains("CONFIGURE_TOTP")) {
                throw new IllegalArgumentException("La configuración de TOTP aún no está disponible desde el portal interno.");
            }

            updatePendingChallenge(session, challenge, currentUser, request.newPassword());
            AdminUserDTO userDTO = establishAuthenticatedSession(authenticateWithPassword(challenge.login(), challengePassword(session)), httpRequest);
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", e.getMessage()));
        } catch (HttpClientErrorException e) {
            LOG.warn("No se pudo finalizar la autenticación posterior a acciones obligatorias para {}", challenge.login(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("detail", resolveAuthenticationDetail(e), "requiresActionCompletion", true));
        } catch (Exception e) {
            LOG.error("Error completando acciones obligatorias para {}", challenge.login(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("detail", "No se pudieron completar las acciones obligatorias."));
        }
    }

    @GetMapping("/authenticate/keycloak")
    public void authenticateWithKeycloak(
        @RequestParam(name = "redirect_uri", required = false) String redirectUri,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        authenticateWithBrowser(redirectUri, request, response);
    }

    @GetMapping("/authenticate/browser")
    public void authenticateWithBrowser(
        @RequestParam(name = "redirect_uri", required = false) String redirectUri,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_ATTR_POST_LOGIN_REDIRECT, sanitizeRedirectUri(redirectUri));
        response.sendRedirect(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/oauth2/authorization/oidc");
    }

    /**
     * {@code POST  /logout} : Logout the current user (BFF session invalidation).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            LOG.info("BFF logout - invalidating session: {}", session.getId());
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    /**
     * Extract authorities/roles from the Keycloak JWT claims.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = new ArrayList<>(SecurityUtils.extractRoleNamesFromClaims(jwt.getClaims()));
        return new ArrayList<>(permissionAuthorityService.buildAuthorities(roles));
    }

    private String resolveAuthenticationDetail(HttpClientErrorException exception) {
        String responseBody = Optional.ofNullable(exception.getResponseBodyAsString()).orElse("").toLowerCase(Locale.ROOT);
        if (responseBody.contains("account is not fully set up")) {
            return "La cuenta tiene acciones pendientes en el flujo de autenticación. Continúa el acceso en el navegador para completar el cambio de contraseña, verificación de correo o cualquier otra acción obligatoria.";
        }
        if (responseBody.contains("account disabled")) {
            return "La cuenta está desactivada.";
        }
        return "Usuario o contraseña incorrectos";
    }

    private boolean requiresBrowserLogin(HttpClientErrorException exception) {
        String responseBody = Optional.ofNullable(exception.getResponseBodyAsString()).orElse("").toLowerCase(Locale.ROOT);
        return responseBody.contains("account is not fully set up");
    }

    private AdminUserDTO establishAuthenticatedSession(Map<String, Object> tokenResponse, HttpServletRequest request) {
        String accessToken = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        Integer expiresIn = (Integer) tokenResponse.get("expires_in");

        Jwt jwt = jwtDecoder.decode(accessToken);
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        clearPendingChallenge(session);
        session.setAttribute(SESSION_ATTR_ACCESS_TOKEN, accessToken);
        session.setAttribute(SESSION_ATTR_REFRESH_TOKEN, refreshToken);
        session.setAttribute(SESSION_ATTR_TOKEN_EXPIRY, System.currentTimeMillis() + (expiresIn * 1000L));

        return userService.getUserFromAuthentication(authentication);
    }

    private Map<String, Object> authenticateWithPassword(String username, String password) {
        String tokenEndpoint = issuerUri + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);
        body.add("scope", "openid profile email offline_access");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(tokenEndpoint, tokenRequest, Map.class);
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new IllegalArgumentException("No se pudo autenticar la sesión con las credenciales actualizadas.");
        }
        return tokenResponse;
    }

    private PendingRequiredActionsChallenge getPendingChallenge(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("La sesión de autenticación pendiente ya no está disponible.");
        }
        Object value = session.getAttribute(SESSION_ATTR_PENDING_CHALLENGE);
        if (value instanceof PendingRequiredActionsChallenge challenge) {
            return challenge;
        }
        throw new IllegalArgumentException("No hay acciones obligatorias pendientes para esta sesión.");
    }

    private void updatePendingChallenge(
        HttpSession session,
        PendingRequiredActionsChallenge currentChallenge,
        KeycloakAdminService.ManagedKeycloakUser currentUser,
        String newPassword
    ) {
        if (session == null) {
            return;
        }
        session.setAttribute(
            SESSION_ATTR_PENDING_CHALLENGE,
            new PendingRequiredActionsChallenge(
                currentUser.id(),
                currentUser.login(),
                currentUser.firstName(),
                currentUser.lastName(),
                currentUser.email(),
                currentUser.requiredActions(),
                newPassword == null || newPassword.isBlank() ? currentChallenge.password() : newPassword
            )
        );
    }

    private String challengePassword(HttpSession session) {
        PendingRequiredActionsChallenge challenge = getPendingChallenge(session);
        if (challenge.password() == null || challenge.password().isBlank()) {
            throw new IllegalArgumentException("No se encontró la credencial temporal para finalizar la autenticación.");
        }
        return challenge.password();
    }

    private void clearPendingChallenge(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_ATTR_PENDING_CHALLENGE);
        }
    }

    private void clearAuthenticatedSession(HttpSession session) {
        clearPendingChallenge(session);
        if (session == null) {
            return;
        }
        session.removeAttribute(SESSION_ATTR_ACCESS_TOKEN);
        session.removeAttribute(SESSION_ATTR_REFRESH_TOKEN);
        session.removeAttribute(SESSION_ATTR_TOKEN_EXPIRY);
        SecurityContextHolder.clearContext();
    }

    private void validateProfileUpdate(CompleteRequiredActionsVM request) {
        if (firstNonBlank(request.firstName(), request.lastName(), request.email()) == null) {
            throw new IllegalArgumentException("Debes completar los datos de perfil requeridos antes de continuar.");
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String sanitizeRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            return "/";
        }
        if (redirectUri.startsWith("/") && !redirectUri.startsWith("//")) {
            return redirectUri;
        }

        URI candidate = URI.create(redirectUri);
        if (!candidate.isAbsolute()) {
            return "/";
        }

        String candidateOrigin = candidate.getScheme() + "://" + candidate.getAuthority();
        Set<String> validOrigins = Arrays.stream(Optional.ofNullable(allowedOrigins).orElse("").split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (validOrigins.contains(candidateOrigin)) {
            return redirectUri;
        }
        LOG.warn("Se rechazó redirect_uri no permitido durante login de Keycloak: {}", redirectUri);
        return "/";
    }

    private record PendingRequiredActionsChallenge(
        String userId,
        String login,
        String firstName,
        String lastName,
        String email,
        List<String> requiredActions,
        String password
    ) {}

    public record CompleteRequiredActionsVM(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @Email @Size(max = 254) String email,
        @Size(max = 255) String newPassword
    ) {}
}
