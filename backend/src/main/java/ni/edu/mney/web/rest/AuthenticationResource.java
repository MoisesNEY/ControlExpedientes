package ni.edu.mney.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.*;
import ni.edu.mney.service.UserService;
import ni.edu.mney.service.dto.AdminUserDTO;
import ni.edu.mney.web.rest.vm.LoginVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    private static final String SESSION_ATTR_ACCESS_TOKEN = "KC_ACCESS_TOKEN";
    private static final String SESSION_ATTR_REFRESH_TOKEN = "KC_REFRESH_TOKEN";
    private static final String SESSION_ATTR_TOKEN_EXPIRY = "KC_TOKEN_EXPIRY";

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    private String clientSecret;

    private final JwtDecoder jwtDecoder;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    public AuthenticationResource(JwtDecoder jwtDecoder, UserService userService) {
        this.jwtDecoder = jwtDecoder;
        this.userService = userService;
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

            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            Integer expiresIn = (Integer) tokenResponse.get("expires_in");

            // 2. Decode the JWT to set up Spring Security context
            Jwt jwt = jwtDecoder.decode(accessToken);
            Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

            JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Store tokens in server-side HttpSession
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_ATTR_ACCESS_TOKEN, accessToken);
            session.setAttribute(SESSION_ATTR_REFRESH_TOKEN, refreshToken);
            session.setAttribute(SESSION_ATTR_TOKEN_EXPIRY, System.currentTimeMillis() + (expiresIn * 1000L));

            // 4. Use the existing UserService to get account info (syncs user in DB)
            AdminUserDTO userDTO = userService.getUserFromAuthentication(authentication);

            LOG.info("BFF authentication successful for user: {}", loginVM.getUsername());
            return ResponseEntity.ok(userDTO);
        } catch (HttpClientErrorException e) {
            LOG.warn("Keycloak authentication failed for user {}: {}", loginVM.getUsername(), e.getStatusCode());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas", "detail", "Usuario o contraseña incorrectos"));
        } catch (Exception e) {
            LOG.error("Unexpected error during BFF authentication for user: {}", loginVM.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno de autenticación"));
        }
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
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract from realm_access.roles (Keycloak standard)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                roles.stream()
                        .filter(role -> role.startsWith("ROLE_"))
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
        }

        return authorities;
    }
}
