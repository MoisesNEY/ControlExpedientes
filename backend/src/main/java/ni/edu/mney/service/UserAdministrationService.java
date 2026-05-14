package ni.edu.mney.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import ni.edu.mney.domain.Authority;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.AuthorityRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.security.KeycloakAdminService;
import ni.edu.mney.service.dto.ManagedUserDTO;
import ni.edu.mney.service.dto.ManagedUserUpsertDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAdministrationService {

    private static final Set<String> SUPPORTED_REQUIRED_ACTIONS = Set.of("UPDATE_PASSWORD", "VERIFY_EMAIL", "UPDATE_PROFILE");

    private final KeycloakAdminService keycloakAdminService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    public UserAdministrationService(
        KeycloakAdminService keycloakAdminService,
        UserRepository userRepository,
        AuthorityRepository authorityRepository
    ) {
        this.keycloakAdminService = keycloakAdminService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    public List<ManagedUserDTO> getAllUsers() {
        List<KeycloakAdminService.ManagedKeycloakUser> users = keycloakAdminService.listUsers();
        syncLocalUsers(users);
        return users.stream().map(this::toDto).toList();
    }

    public ManagedUserDTO createUser(ManagedUserUpsertDTO request) {
        validateRoles(request.roles());
        validateRequiredActions(request.requiredActions());
        KeycloakAdminService.ManagedKeycloakUser user = keycloakAdminService.createUser(
            request.login(),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.activated(),
            request.roles(),
            request.password(),
            request.temporaryPassword(),
            request.requiredActions()
        );
        syncLocalUser(user);
        return toDto(user);
    }

    public ManagedUserDTO updateUser(String userId, ManagedUserUpsertDTO request) {
        validateRoles(request.roles());
        validateRequiredActions(request.requiredActions());
        KeycloakAdminService.ManagedKeycloakUser user = keycloakAdminService.updateUser(
            userId,
            request.login(),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.activated(),
            request.roles(),
            request.password(),
            request.temporaryPassword(),
            request.requiredActions()
        );
        syncLocalUser(user);
        return toDto(user);
    }

    public void deleteUser(String userId) {
        keycloakAdminService.deleteUser(userId);
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }

    private void syncLocalUsers(List<KeycloakAdminService.ManagedKeycloakUser> users) {
        Set<String> synchronizedUserIds = users.stream()
            .map(KeycloakAdminService.ManagedKeycloakUser::id)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        users.forEach(this::syncLocalUser);
        userRepository.findAll().stream()
            .filter(localUser -> !synchronizedUserIds.contains(localUser.getId()))
            .forEach(userRepository::delete);
    }

    private void syncLocalUser(KeycloakAdminService.ManagedKeycloakUser user) {
        User entity = userRepository.findById(user.id()).orElseGet(User::new);
        entity.setId(user.id());
        entity.setLogin(user.login());
        entity.setFirstName(normalizeNullableValue(user.firstName()));
        entity.setLastName(normalizeNullableValue(user.lastName()));
        entity.setEmail(normalizeNullableValue(user.email()));
        entity.setActivated(user.activated());
        entity.setAuthorities(resolveAuthorities(user.roles()));
        userRepository.save(entity);
    }

    private Set<Authority> resolveAuthorities(List<String> roleNames) {
        Set<Authority> authorities = new LinkedHashSet<>();
        if (roleNames == null) {
            return authorities;
        }
        for (String roleName : roleNames) {
            Authority authority = authorityRepository.findById(roleName).orElseGet(() -> {
                Authority created = new Authority();
                created.setName(roleName);
                return authorityRepository.save(created);
            });
            authorities.add(authority);
        }
        return authorities;
    }

    private void validateRoles(List<String> roles) {
        if (roles == null) {
            return;
        }
        List<String> invalid = roles.stream().filter(role -> role == null || !role.startsWith("ROLE_")).toList();
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("Se recibieron roles inválidos: " + invalid);
        }
    }

    private void validateRequiredActions(List<String> requiredActions) {
        if (requiredActions == null) {
            return;
        }
        List<String> invalid = requiredActions.stream()
            .filter(Objects::nonNull)
            .filter(action -> !SUPPORTED_REQUIRED_ACTIONS.contains(action))
            .toList();
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("Se recibieron acciones obligatorias no soportadas: " + invalid);
        }
    }

    private String normalizeNullableValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ManagedUserDTO toDto(KeycloakAdminService.ManagedKeycloakUser user) {
        return new ManagedUserDTO(
            user.id(),
            user.login(),
            user.firstName(),
            user.lastName(),
            user.email(),
            user.activated(),
            user.roles(),
            user.requiredActions()
        );
    }
}
