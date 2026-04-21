package ni.edu.mney.security;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@Component
public class KeycloakAdminService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    private String applicationClientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    private String applicationClientSecret;

    @Value("${application.keycloak-admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${application.keycloak-admin.username:admin}")
    private String adminUsername;

    @Value("${application.keycloak-admin.password:admin}")
    private String adminPassword;

    public void createRole(String roleName, String description, Set<String> permissions, Set<String> compositeRoles) {
        Map<String, Object> payload = baseRolePayload(roleName, description, permissions);
        exchange(adminRealmUrl("/roles"), HttpMethod.POST, payload, Void.class);
        syncRoleComposites(roleName, compositeRoles);
    }

    public void updateRole(String roleName, String description, Set<String> permissions, Set<String> compositeRoles) {
        Map<String, Object> current = getRoleRepresentation(roleName);
        current.put("description", description == null ? "" : description);
        current.put("attributes", roleAttributes(permissions));
        exchange(roleUrl(roleName), HttpMethod.PUT, current, Void.class);
        syncRoleComposites(roleName, compositeRoles);
    }

    public void deleteRole(String roleName) {
        exchange(roleUrl(roleName), HttpMethod.DELETE, null, Void.class);
    }

    public List<String> listRealmRoles() {
        List<?> body = exchange(adminRealmUrl("/roles"), HttpMethod.GET, null, List.class).getBody();
        if (body == null) {
            return List.of();
        }
        return body.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(item -> Objects.toString(item.get("name"), null))
            .filter(name -> name != null && name.startsWith("ROLE_"))
            .sorted()
            .toList();
    }

    public List<ManagedKeycloakUser> listUsers() {
        List<?> users = exchange(adminRealmUrl("/users?max=200"), HttpMethod.GET, null, List.class).getBody();
        if (users == null) {
            return List.of();
        }
        List<ManagedKeycloakUser> result = new ArrayList<>();
        for (Object item : users) {
            if (!(item instanceof Map<?, ?> rawUser)) {
                continue;
            }
            String id = Objects.toString(rawUser.get("id"), null);
            if (id == null) {
                continue;
            }
            result.add(
                new ManagedKeycloakUser(
                    id,
                    Objects.toString(rawUser.get("username"), ""),
                    Objects.toString(rawUser.get("firstName"), ""),
                    Objects.toString(rawUser.get("lastName"), ""),
                    Objects.toString(rawUser.get("email"), ""),
                    Boolean.TRUE.equals(rawUser.get("enabled")),
                    getUserRoles(id),
                    toStringList(rawUser.get("requiredActions"))
                )
            );
        }
        return result.stream().sorted((left, right) -> left.login().compareToIgnoreCase(right.login())).toList();
    }

    public ManagedKeycloakUser createUser(
        String login,
        String firstName,
        String lastName,
        String email,
        boolean activated,
        Collection<String> roles,
        String password,
        boolean temporaryPassword,
        Collection<String> requiredActions
    ) {
        Map<String, Object> payload = userPayload(login, firstName, lastName, email, activated, requiredActions);
        if (password != null && !password.isBlank()) {
            payload.put("credentials", List.of(passwordPayload(password, temporaryPassword)));
        }

        ResponseEntity<Void> response = exchange(adminRealmUrl("/users"), HttpMethod.POST, payload, Void.class);
        String id = extractCreatedId(response.getHeaders().getLocation(), login);
        syncUserRoles(id, roles);
        return findUserById(id);
    }

    public ManagedKeycloakUser updateUser(
        String userId,
        String login,
        String firstName,
        String lastName,
        String email,
        boolean activated,
        Collection<String> roles,
        String password,
        boolean temporaryPassword,
        Collection<String> requiredActions
    ) {
        Map<String, Object> payload = userPayload(login, firstName, lastName, email, activated, requiredActions);
        exchange(userUrl(userId), HttpMethod.PUT, payload, Void.class);
        if (password != null && !password.isBlank()) {
            exchange(userUrl(userId) + "/reset-password", HttpMethod.PUT, passwordPayload(password, temporaryPassword), Void.class);
        }
        syncUserRoles(userId, roles);
        return findUserById(userId);
    }

    public boolean validateUserCredentials(String username, String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", applicationClientId);
            body.add("client_secret", applicationClientSecret);
            body.add("username", username);
            body.add("password", password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<Map> response = restTemplate.exchange(tokenEndpoint(), HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            return response.getBody() != null && response.getBody().containsKey("access_token");
        } catch (Exception error) {
            return false;
        }
    }

    private void syncRoleComposites(String roleName, Collection<String> compositeRoles) {
        Set<String> desired = normalizeRoleNames(compositeRoles);
        Set<String> current = getRoleCompositeNames(roleName);

        List<Map<String, Object>> toAdd = desired.stream().filter(role -> !current.contains(role)).map(this::getRoleRepresentation).toList();
        if (!toAdd.isEmpty()) {
            exchange(roleUrl(roleName) + "/composites", HttpMethod.POST, toAdd, Void.class);
        }

        List<Map<String, Object>> toRemove = current.stream().filter(role -> !desired.contains(role)).map(this::getRoleRepresentation).toList();
        if (!toRemove.isEmpty()) {
            exchange(roleUrl(roleName) + "/composites", HttpMethod.DELETE, toRemove, Void.class);
        }
    }

    private Set<String> getRoleCompositeNames(String roleName) {
        List<?> body = exchange(roleUrl(roleName) + "/composites", HttpMethod.GET, null, List.class).getBody();
        if (body == null) {
            return Set.of();
        }
        return body.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(item -> Objects.toString(item.get("name"), null))
            .filter(name -> name != null && name.startsWith("ROLE_"))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void syncUserRoles(String userId, Collection<String> desiredRoles) {
        Set<String> desired = normalizeRoleNames(desiredRoles);
        Set<String> current = new LinkedHashSet<>(getUserRoles(userId));

        List<Map<String, Object>> toAdd = desired.stream().filter(role -> !current.contains(role)).map(this::getRoleRepresentation).toList();
        if (!toAdd.isEmpty()) {
            exchange(userUrl(userId) + "/role-mappings/realm", HttpMethod.POST, toAdd, Void.class);
        }

        List<Map<String, Object>> toRemove = current.stream().filter(role -> !desired.contains(role)).map(this::getRoleRepresentation).toList();
        if (!toRemove.isEmpty()) {
            exchange(userUrl(userId) + "/role-mappings/realm", HttpMethod.DELETE, toRemove, Void.class);
        }
    }

    private List<String> getUserRoles(String userId) {
        List<?> body = exchange(userUrl(userId) + "/role-mappings/realm", HttpMethod.GET, null, List.class).getBody();
        if (body == null) {
            return List.of();
        }
        return body.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(item -> Objects.toString(item.get("name"), null))
            .filter(name -> name != null && name.startsWith("ROLE_"))
            .sorted()
            .toList();
    }

    private ManagedKeycloakUser findUserById(String userId) {
        Map<?, ?> body = exchange(userUrl(userId), HttpMethod.GET, null, Map.class).getBody();
        if (body == null) {
            throw new IllegalStateException("No se pudo consultar el usuario recién sincronizado en Keycloak.");
        }
        return new ManagedKeycloakUser(
            Objects.toString(body.get("id"), userId),
            Objects.toString(body.get("username"), ""),
            Objects.toString(body.get("firstName"), ""),
            Objects.toString(body.get("lastName"), ""),
            Objects.toString(body.get("email"), ""),
            Boolean.TRUE.equals(body.get("enabled")),
            getUserRoles(userId),
            toStringList(body.get("requiredActions"))
        );
    }

    private String extractCreatedId(URI location, String login) {
        if (location != null) {
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
        List<?> body = exchange(adminRealmUrl("/users?username=" + encode(login)), HttpMethod.GET, null, List.class).getBody();
        if (body != null && !body.isEmpty() && body.get(0) instanceof Map<?, ?> first) {
            return Objects.toString(first.get("id"), null);
        }
        throw new IllegalStateException("No se pudo identificar el usuario creado en Keycloak.");
    }

    private Map<String, Object> baseRolePayload(String roleName, String description, Set<String> permissions) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", roleName);
        payload.put("description", description == null ? "" : description);
        payload.put("clientRole", false);
        payload.put("attributes", roleAttributes(permissions));
        return payload;
    }

    private Map<String, List<String>> roleAttributes(Set<String> permissions) {
        Map<String, List<String>> attributes = new LinkedHashMap<>();
        attributes.put("permissions", permissions == null ? List.of() : permissions.stream().sorted().toList());
        return attributes;
    }

    private Map<String, Object> userPayload(
        String login,
        String firstName,
        String lastName,
        String email,
        boolean activated,
        Collection<String> requiredActions
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", login);
        payload.put("firstName", firstName == null ? "" : firstName);
        payload.put("lastName", lastName == null ? "" : lastName);
        payload.put("email", email == null ? "" : email);
        payload.put("enabled", activated);
        payload.put("requiredActions", requiredActions == null ? List.of() : requiredActions.stream().distinct().toList());
        return payload;
    }

    private Map<String, Object> passwordPayload(String password, boolean temporaryPassword) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "password");
        payload.put("temporary", temporaryPassword);
        payload.put("value", password);
        return payload;
    }

    private Map<String, Object> getRoleRepresentation(String roleName) {
        Map<?, ?> body = exchange(roleUrl(roleName), HttpMethod.GET, null, Map.class).getBody();
        if (body == null) {
            throw new IllegalStateException("No se encontró el rol " + roleName + " en Keycloak.");
        }
        return new LinkedHashMap<>((Map<String, Object>) body);
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod method, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(requestAdminAccessToken());
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), responseType);
    }

    private String requestAdminAccessToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", adminClientId);
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Map> response = restTemplate.exchange(tokenEndpoint(), HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new IllegalStateException("No se pudo obtener un token administrativo de Keycloak.");
        }
        return Objects.toString(response.getBody().get("access_token"));
    }

    private String adminRealmUrl(String path) {
        return keycloakBaseUrl() + "/admin/realms/" + realmName() + path;
    }

    private String roleUrl(String roleName) {
        return adminRealmUrl("/roles/" + encode(roleName));
    }

    private String userUrl(String userId) {
        return adminRealmUrl("/users/" + encode(userId));
    }

    private String tokenEndpoint() {
        return issuerUri + "/protocol/openid-connect/token";
    }

    private String keycloakBaseUrl() {
        int realmIndex = issuerUri.indexOf("/realms/");
        if (realmIndex < 0) {
            throw new IllegalStateException("No se pudo derivar la base de Keycloak desde issuer-uri.");
        }
        return issuerUri.substring(0, realmIndex);
    }

    private String realmName() {
        int realmIndex = issuerUri.indexOf("/realms/");
        if (realmIndex < 0) {
            throw new IllegalStateException("No se pudo derivar el realm de Keycloak desde issuer-uri.");
        }
        return issuerUri.substring(realmIndex + "/realms/".length());
    }

    private String encode(String value) {
        return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
    }

    private Set<String> normalizeRoleNames(Collection<String> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
            .filter(role -> role != null && role.startsWith("ROLE_"))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> toStringList(Object rawValue) {
        if (!(rawValue instanceof Collection<?> collection)) {
            return List.of();
        }
        return collection.stream().map(String::valueOf).distinct().sorted().toList();
    }

    public record ManagedKeycloakUser(
        String id,
        String login,
        String firstName,
        String lastName,
        String email,
        boolean activated,
        List<String> roles,
        List<String> requiredActions
    ) {}
}
