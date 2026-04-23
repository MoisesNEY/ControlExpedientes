package ni.edu.mney.security;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakAdminService.class);
    private static final int MAX_PASSWORD_VALIDATION_FAILURES = 5;
    private static final Duration PASSWORD_VALIDATION_WINDOW = Duration.ofMinutes(5);
    private static final int USER_PAGE_SIZE = 200;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, ValidationAttempt> failedPasswordValidations = new ConcurrentHashMap<>();

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    private String applicationClientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    private String applicationClientSecret;

    @Value("${application.keycloak-admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${application.keycloak-admin.username:}")
    private String adminUsername;

    @Value("${application.keycloak-admin.password:}")
    private String adminPassword;

    public void createRole(String roleName, String description, Set<String> permissions, Set<String> compositeRoles) {
        Map<String, Object> payload = baseRolePayload(roleName, description, permissions);
        exchange(adminRealmUrl("/roles"), HttpMethod.POST, payload, Void.class);
        syncRoleComposites(roleName, compositeRoles);
        ensureRoleGroup(roleName);
    }

    public void updateRole(String roleName, String description, Set<String> permissions, Set<String> compositeRoles) {
        Map<String, Object> current = getRoleRepresentation(roleName);
        current.put("description", description == null ? "" : description);
        current.put("attributes", roleAttributes(permissions));
        exchange(roleUrl(roleName), HttpMethod.PUT, current, Void.class);
        syncRoleComposites(roleName, compositeRoles);
        ensureRoleGroup(roleName);
    }

    public void deleteRole(String roleName) {
        deleteRoleGroup(roleName);
        exchange(roleUrl(roleName), HttpMethod.DELETE, null, Void.class);
    }

    public List<String> listRealmRoles() {
        return listRoles().stream().map(ManagedKeycloakRole::roleName).toList();
    }

    public List<ManagedKeycloakRole> listRoles() {
        List<?> body = exchange(adminRealmUrl("/roles"), HttpMethod.GET, null, List.class).getBody();
        if (body == null) {
            return List.of();
        }
        return body.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(this::toManagedRole)
            .filter(Objects::nonNull)
            .sorted(java.util.Comparator.comparing(ManagedKeycloakRole::roleName))
            .toList();
    }

    public List<ManagedKeycloakUser> listUsers() {
        List<Object> rawUsers = new ArrayList<>();
        for (int first = 0;; first += USER_PAGE_SIZE) {
            List<?> page = exchange(adminRealmUrl("/users?first=" + first + "&max=" + USER_PAGE_SIZE), HttpMethod.GET, null, List.class).getBody();
            if (page == null || page.isEmpty()) {
                break;
            }
            rawUsers.addAll(page);
            if (page.size() < USER_PAGE_SIZE) {
                break;
            }
        }
        List<ManagedKeycloakUser> result = new ArrayList<>();
        for (Object item : rawUsers) {
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
        if (isTemporarilyBlocked(username)) {
            LOG.warn("Se bloqueó temporalmente la revalidación de credenciales para {}", username);
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
            boolean valid = response.getBody() != null && response.getBody().containsKey("access_token");
            if (valid) {
                failedPasswordValidations.remove(username);
            } else {
                registerFailedValidation(username);
            }
            return valid;
        } catch (Exception error) {
            registerFailedValidation(username);
            LOG.warn("Falló la revalidación de credenciales para {}", username);
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
        return extractRoleNames(body);
    }

    private void syncUserRoles(String userId, Collection<String> desiredRoles) {
        Set<String> desired = normalizeRoleNames(desiredRoles);
        Map<String, String> currentGroups = getUserRoleGroups(userId);

        for (String roleName : desired) {
            if (currentGroups.containsKey(roleName)) {
                continue;
            }
            String groupId = ensureRoleGroup(roleName);
            exchange(userUrl(userId) + "/groups/" + encode(groupId), HttpMethod.PUT, null, Void.class);
        }

        for (Map.Entry<String, String> currentGroup : currentGroups.entrySet()) {
            if (desired.contains(currentGroup.getKey())) {
                continue;
            }
            exchange(userUrl(userId) + "/groups/" + encode(currentGroup.getValue()), HttpMethod.DELETE, null, Void.class);
        }

        clearDirectUserRoles(userId);
    }

    private List<String> getUserRoles(String userId) {
        Set<String> roles = new LinkedHashSet<>(getUserDirectRoles(userId));
        roles.addAll(getUserRoleGroups(userId).keySet());
        return roles.stream().sorted().toList();
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
        return extractCreatedId(location, login, value -> {
            List<?> body = exchange(adminRealmUrl("/users?username=" + encode(value)), HttpMethod.GET, null, List.class).getBody();
            if (body != null && !body.isEmpty() && body.get(0) instanceof Map<?, ?> first) {
                return Objects.toString(first.get("id"), null);
            }
            return null;
        });
    }

    private String extractCreatedId(URI location, String lookupKey, java.util.function.Function<String, String> fallbackLookup) {
        if (location != null) {
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
        String lookedUpId = fallbackLookup.apply(lookupKey);
        if (lookedUpId != null && !lookedUpId.isBlank()) {
            return lookedUpId;
        }
        throw new IllegalStateException("No se pudo identificar el recurso creado en Keycloak.");
    }

    private Map<String, Object> baseRolePayload(String roleName, String description, Set<String> permissions) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", roleName);
        payload.put("description", description == null ? "" : description);
        payload.put("clientRole", false);
        payload.put("attributes", roleAttributes(permissions));
        return payload;
    }

    private ManagedKeycloakRole toManagedRole(Map<?, ?> rawRole) {
        String roleName = Objects.toString(rawRole.get("name"), null);
        if (roleName == null || !roleName.startsWith("ROLE_") || Boolean.TRUE.equals(rawRole.get("clientRole"))) {
            return null;
        }
        return new ManagedKeycloakRole(
            roleName,
            Objects.toString(rawRole.get("description"), ""),
            extractRolePermissions(rawRole.get("attributes")),
            getRoleCompositeNames(roleName)
        );
    }

    private Set<String> extractRolePermissions(Object rawAttributes) {
        if (!(rawAttributes instanceof Map<?, ?> attributes)) {
            return Set.of();
        }
        Object permissions = attributes.get("permissions");
        if (!(permissions instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
            .map(String::valueOf)
            .filter(permission -> permission != null && !permission.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));
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
        return safeObjectMap(body);
    }

    private String ensureRoleGroup(String roleName) {
        String existingGroupId = findRoleGroupId(roleName);
        if (existingGroupId != null) {
            syncGroupRealmRoles(existingGroupId, Set.of(roleName));
            return existingGroupId;
        }

        ResponseEntity<Void> response = exchange(adminRealmUrl("/groups"), HttpMethod.POST, Map.of("name", roleName), Void.class);
        String createdGroupId = extractCreatedId(response.getHeaders().getLocation(), roleName, this::findRoleGroupId);
        syncGroupRealmRoles(createdGroupId, Set.of(roleName));
        return createdGroupId;
    }

    private void deleteRoleGroup(String roleName) {
        String groupId = findRoleGroupId(roleName);
        if (groupId != null) {
            exchange(groupUrl(groupId), HttpMethod.DELETE, null, Void.class);
        }
    }

    private void syncGroupRealmRoles(String groupId, Set<String> desiredRoles) {
        Set<String> desired = normalizeRoleNames(desiredRoles);
        Set<String> current = getGroupRealmRoles(groupId);

        List<Map<String, Object>> toAdd = desired.stream().filter(role -> !current.contains(role)).map(this::getRoleRepresentation).toList();
        if (!toAdd.isEmpty()) {
            exchange(groupUrl(groupId) + "/role-mappings/realm", HttpMethod.POST, toAdd, Void.class);
        }

        List<Map<String, Object>> toRemove = current.stream().filter(role -> !desired.contains(role)).map(this::getRoleRepresentation).toList();
        if (!toRemove.isEmpty()) {
            exchange(groupUrl(groupId) + "/role-mappings/realm", HttpMethod.DELETE, toRemove, Void.class);
        }
    }

    private Set<String> getGroupRealmRoles(String groupId) {
        List<?> body = exchange(groupUrl(groupId) + "/role-mappings/realm", HttpMethod.GET, null, List.class).getBody();
        return extractRoleNames(body);
    }

    private Map<String, String> getUserRoleGroups(String userId) {
        List<?> body = exchange(userUrl(userId) + "/groups", HttpMethod.GET, null, List.class).getBody();
        if (body == null) {
            return Map.of();
        }

        Map<String, String> groups = new LinkedHashMap<>();
        for (Object item : body) {
            if (!(item instanceof Map<?, ?> rawGroup)) {
                continue;
            }
            String groupName = Objects.toString(rawGroup.get("name"), null);
            String groupId = Objects.toString(rawGroup.get("id"), null);
            if (groupName == null || groupId == null || !groupName.startsWith("ROLE_")) {
                continue;
            }
            groups.put(groupName, groupId);
        }
        return groups;
    }

    private void clearDirectUserRoles(String userId) {
        List<Map<String, Object>> directRoles = getUserDirectRoleRepresentations(userId);
        if (!directRoles.isEmpty()) {
            exchange(userUrl(userId) + "/role-mappings/realm", HttpMethod.DELETE, directRoles, Void.class);
        }
    }

    private List<String> getUserDirectRoles(String userId) {
        return extractRoleNames(exchange(userUrl(userId) + "/role-mappings/realm", HttpMethod.GET, null, List.class).getBody())
            .stream()
            .sorted()
            .toList();
    }

    private List<Map<String, Object>> getUserDirectRoleRepresentations(String userId) {
        List<?> body = exchange(userUrl(userId) + "/role-mappings/realm", HttpMethod.GET, null, List.class).getBody();
        if (body == null) {
            return List.of();
        }
        return body.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(this::safeObjectMap)
            .filter(item -> {
                String name = Objects.toString(item.get("name"), null);
                return name != null && name.startsWith("ROLE_");
            })
            .toList();
    }

    private String findRoleGroupId(String roleName) {
        List<?> groups = exchange(adminRealmUrl("/groups?search=" + encode(roleName)), HttpMethod.GET, null, List.class).getBody();
        if (groups == null) {
            return null;
        }
        for (Object item : groups) {
            if (!(item instanceof Map<?, ?> rawGroup)) {
                continue;
            }
            if (!roleName.equals(Objects.toString(rawGroup.get("name"), null))) {
                continue;
            }
            return Objects.toString(rawGroup.get("id"), null);
        }
        return null;
    }

    private String groupUrl(String groupId) {
        return adminRealmUrl("/groups/" + encode(groupId));
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
        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("Configura KEYCLOAK_ADMIN_USERNAME y KEYCLOAK_ADMIN_PASSWORD para administrar roles y usuarios.");
        }
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

    private Set<String> extractRoleNames(Object rawValue) {
        if (!(rawValue instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(item -> Objects.toString(item.get("name"), null))
            .filter(name -> name != null && name.startsWith("ROLE_"))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, Object> safeObjectMap(Map<?, ?> source) {
        Map<String, Object> converted = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                continue;
            }
            converted.put(key, entry.getValue());
        }
        return converted;
    }

    private boolean isTemporarilyBlocked(String username) {
        ValidationAttempt attempt = failedPasswordValidations.get(username);
        if (attempt == null) {
            return false;
        }
        if (attempt.expiresAt().isBefore(Instant.now())) {
            failedPasswordValidations.remove(username);
            return false;
        }
        return attempt.failures() >= MAX_PASSWORD_VALIDATION_FAILURES;
    }

    private void registerFailedValidation(String username) {
        failedPasswordValidations.compute(username, (key, current) -> {
            Instant now = Instant.now();
            if (current == null || current.expiresAt().isBefore(now)) {
                return new ValidationAttempt(1, now.plus(PASSWORD_VALIDATION_WINDOW));
            }
            return new ValidationAttempt(current.failures() + 1, current.expiresAt());
        });
    }

    private record ValidationAttempt(int failures, Instant expiresAt) {}

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

    public record ManagedKeycloakRole(String roleName, String description, Set<String> permissions, Set<String> compositeRoles) {}
}
