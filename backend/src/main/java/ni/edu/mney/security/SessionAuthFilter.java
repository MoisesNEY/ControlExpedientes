package ni.edu.mney.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that restores the Spring Security context from the server-side
 * HttpSession.
 * <p>
 * On each request, if a session exists with stored Keycloak tokens, this
 * filter:
 * 1. Reads the access_token from the session.
 * 2. If expired, refreshes it using the refresh_token.
 * 3. Decodes the JWT and sets the SecurityContext authentication.
 * </p>
 * This is the core of the BFF pattern — the frontend only sends a session
 * cookie,
 * and this filter translates it into a valid JWT authentication internally.
 */
@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SessionAuthFilter.class);

    private static final String SESSION_ATTR_ACCESS_TOKEN = "KC_ACCESS_TOKEN";
    private static final String SESSION_ATTR_REFRESH_TOKEN = "KC_REFRESH_TOKEN";
    private static final String SESSION_ATTR_TOKEN_EXPIRY = "KC_TOKEN_EXPIRY";

    private final JwtDecoder jwtDecoder;
    private final PermissionAuthorityService permissionAuthorityService;

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public SessionAuthFilter(@Lazy JwtDecoder jwtDecoder, PermissionAuthorityService permissionAuthorityService) {
        this.jwtDecoder = jwtDecoder;
        this.permissionAuthorityService = permissionAuthorityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip if already authenticated with a real (non-anonymous) principal
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            LOG.debug("SessionAuthFilter: No session for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = (String) session.getAttribute(SESSION_ATTR_ACCESS_TOKEN);
        if (accessToken == null) {
            LOG.debug("SessionAuthFilter: Session exists but no token for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        LOG.debug("SessionAuthFilter: Found token in session for {}", request.getRequestURI());

        // Check if token is about to expire (30s buffer)
        Long tokenExpiry = (Long) session.getAttribute(SESSION_ATTR_TOKEN_EXPIRY);
        if (tokenExpiry != null && System.currentTimeMillis() > (tokenExpiry - 30_000)) {
            accessToken = refreshAccessToken(session);
            if (accessToken == null) {
                // Refresh failed — session is invalid, clear it
                session.invalidate();
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            LOG.warn("Failed to decode access token from session, attempting refresh: {}", e.getMessage());
            // Try refresh once more
            accessToken = refreshAccessToken(session);
            if (accessToken != null) {
                try {
                    Jwt jwt = jwtDecoder.decode(accessToken);
                    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (JwtException retryEx) {
                    LOG.error("Token refresh succeeded but decode still failed, invalidating session");
                    session.invalidate();
                }
            } else {
                session.invalidate();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Refresh the access token using the stored refresh_token.
     *
     * @return the new access token, or null if refresh failed.
     */
    private String refreshAccessToken(HttpSession session) {
        String refreshToken = (String) session.getAttribute(SESSION_ATTR_REFRESH_TOKEN);
        if (refreshToken == null) {
            return null;
        }

        try {
            String tokenEndpoint = issuerUri + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(tokenEndpoint, tokenRequest, Map.class);

            if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
                String newAccessToken = (String) tokenResponse.get("access_token");
                String newRefreshToken = (String) tokenResponse.get("refresh_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");

                session.setAttribute(SESSION_ATTR_ACCESS_TOKEN, newAccessToken);
                if (newRefreshToken != null) {
                    session.setAttribute(SESSION_ATTR_REFRESH_TOKEN, newRefreshToken);
                }
                session.setAttribute(SESSION_ATTR_TOKEN_EXPIRY, System.currentTimeMillis() + (expiresIn * 1000L));

                LOG.debug("Successfully refreshed access token");
                return newAccessToken;
            }
        } catch (Exception e) {
            LOG.warn("Failed to refresh access token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract authorities/roles from the Keycloak JWT claims.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = realmAccess == null
            ? List.of()
            : ((List<?>) realmAccess.getOrDefault("roles", List.of())).stream()
                .map(String::valueOf)
                .filter(role -> role.startsWith("ROLE_"))
                .collect(Collectors.toList());
        return new ArrayList<>(permissionAuthorityService.buildAuthorities(roles));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Don't filter login, public, and static resource requests
        return path.equals("/api/authenticate") ||
                path.equals("/api/auth-info") ||
                path.startsWith("/management/") ||
                path.startsWith("/v3/api-docs");
    }
}
