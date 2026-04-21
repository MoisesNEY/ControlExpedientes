package ni.edu.mney.service;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import ni.edu.mney.domain.Authority;
import ni.edu.mney.domain.RoleDefinition;
import ni.edu.mney.repository.AuthorityRepository;
import ni.edu.mney.repository.RoleDefinitionRepository;
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

    private static final List<String> SYSTEM_ROLES = List.of(
        AuthoritiesConstants.ADMIN,
        AuthoritiesConstants.MEDICO,
        AuthoritiesConstants.ENFERMERO,
        AuthoritiesConstants.RECEPCION,
        AuthoritiesConstants.USER
    );

    private final RoleDefinitionRepository roleDefinitionRepository;
    private final AuthorityRepository authorityRepository;
    private final KeycloakAdminService keycloakAdminService;

    public RoleAdministrationService(
        RoleDefinitionRepository roleDefinitionRepository,
        AuthorityRepository authorityRepository,
        KeycloakAdminService keycloakAdminService
    ) {
        this.roleDefinitionRepository = roleDefinitionRepository;
        this.authorityRepository = authorityRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @PostConstruct
    void initializeDefaults() {
        ensureSystemRole(AuthoritiesConstants.ADMIN, "Administrador del sistema", AppPermissionCatalog.allCodes());
        ensureSystemRole(AuthoritiesConstants.MEDICO, "Acceso clínico para personal médico", Set.of());
        ensureSystemRole(AuthoritiesConstants.ENFERMERO, "Acceso operativo para enfermería", Set.of());
        ensureSystemRole(AuthoritiesConstants.RECEPCION, "Acceso operativo para recepción", Set.of());
        ensureSystemRole(AuthoritiesConstants.USER, "Rol base de plataforma", Set.of());
    }

    @Transactional(readOnly = true)
    public List<RoleDefinitionDTO> getAllRoles() {
        return roleDefinitionRepository.findAll().stream()
            .sorted(Comparator.comparing(RoleDefinition::getRoleName))
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public RoleManagementCatalogDTO getCatalog() {
        List<PermissionDefinitionDTO> permissions = AppPermissionCatalog.all().stream()
            .map(this::toPermissionDto)
            .toList();
        return new RoleManagementCatalogDTO(SYSTEM_ROLES, permissions);
    }

    public RoleDefinitionDTO createRole(RoleUpsertDTO request) {
        validatePermissionCodes(request.permissions());
        if (roleDefinitionRepository.existsById(request.roleName())) {
            throw new IllegalArgumentException("El rol ya existe.");
        }
        ensureAuthority(request.roleName());

        boolean roleCreatedInKeycloak = false;
        try {
            keycloakAdminService.createRole(
                request.roleName(),
                request.description(),
                normalizePermissions(request.permissions()),
                normalizeCompositeRoles(request.compositeRoles())
            );
            roleCreatedInKeycloak = true;
            return toDto(persistRole(request, false));
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
        RoleDefinition existing = roleDefinitionRepository.findById(roleName)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol solicitado."));
        if (existing.isSystemRole()) {
            throw new IllegalArgumentException("Los roles del sistema no se pueden editar desde este módulo.");
        }
        validatePermissionCodes(request.permissions());
        keycloakAdminService.updateRole(roleName, request.description(), normalizePermissions(request.permissions()), normalizeCompositeRoles(request.compositeRoles()));
        existing.setDescription(request.description());
        existing.setCompositeRoles(normalizeCompositeRoles(request.compositeRoles()));
        existing.setPermissions(normalizePermissions(request.permissions()));
        return toDto(roleDefinitionRepository.save(existing));
    }

    public void deleteRole(String roleName) {
        RoleDefinition existing = roleDefinitionRepository.findById(roleName)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol solicitado."));
        if (existing.isSystemRole()) {
            throw new IllegalArgumentException("Los roles del sistema no se pueden eliminar.");
        }
        keycloakAdminService.deleteRole(roleName);
        roleDefinitionRepository.delete(existing);
        authorityRepository.deleteById(roleName);
    }

    private void ensureSystemRole(String roleName, String description, Set<String> permissions) {
        ensureAuthority(roleName);
        roleDefinitionRepository.findById(roleName).orElseGet(() -> {
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

    private RoleDefinition persistRole(RoleUpsertDTO request, boolean systemRole) {
        RoleDefinition definition = new RoleDefinition();
        definition.setRoleName(request.roleName());
        definition.setDescription(request.description());
        definition.setSystemRole(systemRole);
        definition.setCompositeRoles(normalizeCompositeRoles(request.compositeRoles()));
        definition.setPermissions(normalizePermissions(request.permissions()));
        return roleDefinitionRepository.save(definition);
    }

    private Set<String> normalizeCompositeRoles(Set<String> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
            .filter(SYSTEM_ROLES::contains)
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
}
