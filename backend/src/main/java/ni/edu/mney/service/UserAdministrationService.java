package ni.edu.mney.service;

import java.util.LinkedHashSet;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<ManagedUserDTO> getAllUsers() {
        return keycloakAdminService.listUsers().stream().map(this::toDto).toList();
    }

    public ManagedUserDTO createUser(ManagedUserUpsertDTO request) {
        validateRoles(request.roles());
        validateLoginCompatibleProvisioning(request);
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
        validateLoginCompatibleProvisioning(request);
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

    private void syncLocalUser(KeycloakAdminService.ManagedKeycloakUser user) {
        User entity = userRepository.findById(user.id()).orElseGet(User::new);
        entity.setId(user.id());
        entity.setLogin(user.login());
        entity.setFirstName(user.firstName());
        entity.setLastName(user.lastName());
        entity.setEmail(user.email());
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

    private void validateLoginCompatibleProvisioning(ManagedUserUpsertDTO request) {
        boolean hasRequiredActions = request.requiredActions() != null && request.requiredActions().stream().anyMatch(action -> action != null && !action.isBlank());
        if (request.temporaryPassword() || hasRequiredActions) {
            throw new IllegalArgumentException(
                "Esta aplicación no admite contraseñas temporales ni acciones obligatorias de Keycloak en el inicio de sesión. Asigna una contraseña permanente y deja vacías las acciones obligatorias."
            );
        }
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
