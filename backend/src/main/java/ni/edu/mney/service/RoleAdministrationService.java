package ni.edu.mney.service;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import ni.edu.mney.domain.Authority;
import ni.edu.mney.domain.RoleDefinition;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.AuthorityRepository;
import ni.edu.mney.repository.RoleDefinitionRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.security.AppPermission;
import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.security.KeycloakAdminService;
import ni.edu.mney.service.dto.PermissionDefinitionDTO;
import ni.edu.mney.service.dto.RoleDefinitionDTO;
import ni.edu.mney.service.dto.RoleManagementCatalogDTO;
import ni.edu.mney.service.dto.RoleUpsertDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleAdministrationService {

    private static final Map<String, SystemRoleDefaults> SYSTEM_ROLE_DEFAULTS = Map.of(
        AuthoritiesConstants.ADMIN,
        new SystemRoleDefaults("Administrador del sistema", AppPermissionCatalog.allCodes()),
        AuthoritiesConstants.MEDICO,
        new SystemRoleDefaults("Acceso clínico para personal médico", Set.of()),
        AuthoritiesConstants.ENFERMERO,
        new SystemRoleDefaults("Acceso operativo para enfermería", Set.of()),
        AuthoritiesConstants.RECEPCION,
        new SystemRoleDefaults("Acceso operativo para recepción", Set.of()),
        AuthoritiesConstants.USER,
        new SystemRoleDefaults("Rol base de plataforma", Set.of())
    );

    private final RoleDefinitionRepository roleDefinitionRepository;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    public RoleAdministrationService(
        RoleDefinitionRepository roleDefinitionRepository,
        AuthorityRepository authorityRepository,
        UserRepository userRepository,
        KeycloakAdminService keycloakAdminService
    ) {
        this.roleDefinitionRepository = roleDefinitionRepository;
        this.authorityRepository = authorityRepository;
        this.userRepository = userRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @PostConstruct
    void initializeDefaults() {
        SYSTEM_ROLE_DEFAULTS.forEach((roleName, defaults) -> ensureSystemRole(roleName, defaults.description(), defaults.permissions()));
    }

    public List<RoleDefinitionDTO> getAllRoles() {
        return synchronizeRolesFromKeycloak().stream()
            .sorted(Comparator.comparing(RoleDefinition::getRoleName))
            .map(this::toDto)
            .toList();
    }

    public RoleManagementCatalogDTO getCatalog() {
        List<RoleDefinition> roles = synchronizeRolesFromKeycloak();
        List<PermissionDefinitionDTO> permissions = AppPermissionCatalog.all().stream()
            .map(this::toPermissionDto)
            .toList();
        return new RoleManagementCatalogDTO(roles.stream().map(RoleDefinition::getRoleName).sorted().toList(), permissions);
    }

    public RoleDefinitionDTO createRole(RoleUpsertDTO request) {
        validatePermissionCodes(request.permissions());
        Set<String> existingRoleNames = synchronizeRolesFromKeycloak().stream().map(RoleDefinition::getRoleName).collect(Collectors.toSet());
        if (existingRoleNames.contains(request.roleName())) {
            throw new IllegalArgumentException("El rol ya existe.");
        }
        validateCompositeRoles(request.compositeRoles(), request.roleName(), existingRoleNames);
        ensureAuthority(request.roleName());

        boolean roleCreatedInKeycloak = false;
        try {
            keycloakAdminService.createRole(
                request.roleName(),
                request.description(),
                normalizePermissions(request.permissions()),
                normalizeCompositeRoles(request.compositeRoles(), request.roleName())
            );
            roleCreatedInKeycloak = true;
            return synchronizeRolesFromKeycloak().stream()
                .filter(definition -> definition.getRoleName().equals(request.roleName()))
                .findFirst()
                .map(this::toDto)
                .orElseThrow(() -> new IllegalStateException("No se pudo sincronizar el rol recién creado."));
        } catch (RuntimeException exception) {
            if (roleCreatedInKeycloak) {
                try {
                    keycloakAdminService.deleteRole(request.roleName());
                } catch (RuntimeException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
            }
            throw exception;
        }
    }

    public RoleDefinitionDTO updateRole(String roleName, RoleUpsertDTO request) {
        Map<String, RoleDefinition> synchronizedRoles = synchronizeRolesFromKeycloak().stream()
            .collect(Collectors.toMap(RoleDefinition::getRoleName, definition -> definition, (left, right) -> right, LinkedHashMap::new));
        RoleDefinition existing = synchronizedRoles.containsKey(roleName)
            ? synchronizedRoles.get(roleName)
            : roleDefinitionRepository.findById(roleName).orElseThrow(() -> new IllegalArgumentException("No se encontró el rol solicitado."));
        if (existing == null) {
            throw new IllegalArgumentException("No se encontró el rol solicitado.");
        }
        if (existing.isSystemRole()) {
            throw new IllegalArgumentException("Los roles del sistema no se pueden editar desde este módulo.");
        }
        validatePermissionCodes(request.permissions());
        validateCompositeRoles(request.compositeRoles(), roleName, synchronizedRoles.keySet());
        keycloakAdminService.updateRole(
            roleName,
            request.description(),
            normalizePermissions(request.permissions()),
            normalizeCompositeRoles(request.compositeRoles(), roleName)
        );
        return synchronizeRolesFromKeycloak().stream()
            .filter(definition -> definition.getRoleName().equals(roleName))
            .findFirst()
            .map(this::toDto)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol solicitado."));
    }

    public void deleteRole(String roleName) {
        RoleDefinition existing = synchronizeRolesFromKeycloak().stream()
            .filter(definition -> definition.getRoleName().equals(roleName))
            .findFirst()
            .or(() -> roleDefinitionRepository.findById(roleName))
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol solicitado."));
        if (existing.isSystemRole()) {
            throw new IllegalArgumentException("Los roles del sistema no se pueden eliminar.");
        }
        keycloakAdminService.deleteRole(roleName);
        synchronizeRolesFromKeycloak();
    }

    private void ensureSystemRole(String roleName, String description, Set<String> permissions) {
        ensureAuthority(roleName);
        roleDefinitionRepository.findById(roleName).map(existing -> {
            boolean changed = false;

            if (!existing.isSystemRole()) {
                existing.setSystemRole(true);
                changed = true;
            }
            if (!Objects.equals(existing.getDescription(), description)) {
                existing.setDescription(description);
                changed = true;
            }
            if (!existing.getPermissions().equals(permissions)) {
                existing.setPermissions(permissions);
                changed = true;
            }
            if (!existing.getCompositeRoles().isEmpty()) {
                existing.setCompositeRoles(Set.of());
                changed = true;
            }

            return changed ? roleDefinitionRepository.save(existing) : existing;
        }).orElseGet(() -> {
            RoleDefinition definition = new RoleDefinition();
            definition.setRoleName(roleName);
            definition.setDescription(description);
            definition.setSystemRole(true);
            definition.setPermissions(permissions);
            definition.setCompositeRoles(Set.of());
            return roleDefinitionRepository.save(definition);
        });
    }

    private void ensureAuthority(String roleName) {
        if (authorityRepository.existsById(roleName)) {
            return;
        }
        Authority authority = new Authority();
        authority.setName(roleName);
        authorityRepository.save(authority);
    }

    private Set<String> normalizeCompositeRoles(Set<String> roles, String currentRoleName) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(role -> role.startsWith("ROLE_"))
            .filter(role -> !role.equals(currentRoleName))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> normalizePermissions(Set<String> permissions) {
        if (permissions == null) {
            return Set.of();
        }
        return permissions.stream()
            .filter(AppPermissionCatalog::exists)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validatePermissionCodes(Set<String> permissions) {
        if (permissions == null) {
            return;
        }
        List<String> invalidPermissions = permissions.stream().filter(permission -> !AppPermissionCatalog.exists(permission)).toList();
        if (!invalidPermissions.isEmpty()) {
            throw new IllegalArgumentException("Se recibieron permisos no válidos: " + invalidPermissions);
        }
    }

    private void validateCompositeRoles(Set<String> compositeRoles, String currentRoleName, Set<String> availableRoleNames) {
        if (compositeRoles == null) {
            return;
        }
        List<String> invalidRoles = compositeRoles.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(role -> !role.isBlank())
            .filter(role -> !role.equals(currentRoleName))
            .filter(role -> !availableRoleNames.contains(role))
            .toList();
        if (!invalidRoles.isEmpty()) {
            throw new IllegalArgumentException("Se recibieron roles compuestos no válidos: " + invalidRoles);
        }
    }

    private List<RoleDefinition> synchronizeRolesFromKeycloak() {
        List<KeycloakAdminService.ManagedKeycloakRole> keycloakRoles = keycloakAdminService.listRoles();
        Map<String, RoleDefinition> localDefinitions = roleDefinitionRepository.findAll().stream()
            .collect(Collectors.toMap(RoleDefinition::getRoleName, definition -> definition, (left, right) -> right, LinkedHashMap::new));
        Set<String> synchronizedRoleNames = keycloakRoles.stream()
            .map(KeycloakAdminService.ManagedKeycloakRole::roleName)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        for (KeycloakAdminService.ManagedKeycloakRole keycloakRole : keycloakRoles) {
            ensureAuthority(keycloakRole.roleName());
            RoleDefinition definition = localDefinitions.getOrDefault(keycloakRole.roleName(), new RoleDefinition());
            SystemRoleDefaults systemDefaults = SYSTEM_ROLE_DEFAULTS.get(keycloakRole.roleName());
            definition.setRoleName(keycloakRole.roleName());
            definition.setDescription(systemDefaults != null ? systemDefaults.description() : keycloakRole.description());
            definition.setSystemRole(systemDefaults != null);
            definition.setPermissions(systemDefaults != null ? systemDefaults.permissions() : keycloakRole.permissions());
            definition.setCompositeRoles(systemDefaults != null ? Set.of() : keycloakRole.compositeRoles());
            roleDefinitionRepository.save(definition);
        }

        removeStaleAuthorities(synchronizedRoleNames);

        if (synchronizedRoleNames.isEmpty()) {
            return List.of();
        }
        return roleDefinitionRepository.findAllByRoleNameIn(synchronizedRoleNames).stream()
            .sorted(Comparator.comparing(RoleDefinition::getRoleName))
            .toList();
    }

    private void removeStaleAuthorities(Set<String> synchronizedRoleNames) {
        Set<String> staleAuthorities = authorityRepository.findAll().stream()
            .map(Authority::getName)
            .filter(name -> name.startsWith("ROLE_"))
            .filter(name -> !synchronizedRoleNames.contains(name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (staleAuthorities.isEmpty()) {
            return;
        }

        List<RoleDefinition> staleDefinitions = roleDefinitionRepository.findAllByRoleNameIn(staleAuthorities);
        if (!staleDefinitions.isEmpty()) {
            roleDefinitionRepository.deleteAll(staleDefinitions);
        }

        for (User user : userRepository.findAll()) {
            Set<Authority> updatedAuthorities = user.getAuthorities().stream()
                .filter(authority -> !staleAuthorities.contains(authority.getName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
            if (updatedAuthorities.size() != user.getAuthorities().size()) {
                user.setAuthorities(updatedAuthorities);
                userRepository.save(user);
            }
        }

        staleAuthorities.forEach(authorityRepository::deleteById);
    }

    private RoleDefinitionDTO toDto(RoleDefinition definition) {
        return new RoleDefinitionDTO(
            definition.getRoleName(),
            definition.getDescription(),
            definition.isSystemRole(),
            definition.getCompositeRoles(),
            definition.getPermissions()
        );
    }

    private PermissionDefinitionDTO toPermissionDto(AppPermission permission) {
        return new PermissionDefinitionDTO(permission.code(), permission.label(), permission.description());
    }

    private record SystemRoleDefaults(String description, Set<String> permissions) {}
}
