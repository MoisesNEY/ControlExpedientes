package ni.edu.mney.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import ni.edu.mney.domain.Authority;
import ni.edu.mney.domain.RoleDefinition;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.AuthorityRepository;
import ni.edu.mney.repository.RoleDefinitionRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.security.KeycloakAdminService;
import ni.edu.mney.service.dto.RoleDefinitionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleAdministrationServiceTest {

    @Mock
    private RoleDefinitionRepository roleDefinitionRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private RoleAdministrationService roleAdministrationService;

    @Test
    void initializeDefaultsUpdatesExistingAdminRolePermissions() {
        Map<String, RoleDefinition> storedRoles = new HashMap<>();
        storedRoles.put(
            AuthoritiesConstants.ADMIN,
            roleDefinition(AuthoritiesConstants.ADMIN, "Administrador heredado", true, Set.of(), Set.of(AuthoritiesConstants.USER))
        );
        storedRoles.put(
            AuthoritiesConstants.MEDICO,
            roleDefinition(AuthoritiesConstants.MEDICO, "Acceso clínico para personal médico", true, Set.of(), Set.of())
        );
        storedRoles.put(
            AuthoritiesConstants.ENFERMERO,
            roleDefinition(AuthoritiesConstants.ENFERMERO, "Acceso operativo para enfermería", true, Set.of(), Set.of())
        );
        storedRoles.put(
            AuthoritiesConstants.RECEPCION,
            roleDefinition(AuthoritiesConstants.RECEPCION, "Acceso operativo para recepción", true, Set.of(), Set.of())
        );
        storedRoles.put(AuthoritiesConstants.USER, roleDefinition(AuthoritiesConstants.USER, "Rol base de plataforma", true, Set.of(), Set.of()));

        when(authorityRepository.existsById(anyString())).thenReturn(true);
        when(roleDefinitionRepository.findById(anyString())).thenAnswer(invocation -> Optional.ofNullable(storedRoles.get(invocation.getArgument(0))));
        when(roleDefinitionRepository.save(any(RoleDefinition.class))).thenAnswer(invocation -> {
            RoleDefinition saved = invocation.getArgument(0);
            storedRoles.put(saved.getRoleName(), saved);
            return saved;
        });

        roleAdministrationService.initializeDefaults();

        RoleDefinition adminRole = storedRoles.get(AuthoritiesConstants.ADMIN);
        assertThat(adminRole.getDescription()).isEqualTo("Administrador del sistema");
        assertThat(adminRole.isSystemRole()).isTrue();
        assertThat(adminRole.getCompositeRoles()).isEmpty();
        assertThat(adminRole.getPermissions()).containsExactlyInAnyOrderElementsOf(AppPermissionCatalog.allCodes());

        verify(roleDefinitionRepository).save(anyUpdatedAdminRole());
    }

    @Test
    void getAllRolesSynchronizesWithKeycloakAndRemovesStaleAuthorities() {
        Map<String, RoleDefinition> storedRoles = new HashMap<>();
        storedRoles.put("ROLE_OBSOLETO", roleDefinition("ROLE_OBSOLETO", "Obsoleto", false, Set.of(), Set.of()));

        Map<String, Authority> storedAuthorities = new HashMap<>();
        storedAuthorities.put("ROLE_OBSOLETO", authority("ROLE_OBSOLETO"));

        User staleUser = new User();
        staleUser.setId("stale-user");
        staleUser.setLogin("stale");
        staleUser.setAuthorities(new java.util.LinkedHashSet<>(Set.of(authority("ROLE_OBSOLETO"))));
        AtomicReference<User> updatedUser = new AtomicReference<>(staleUser);

        when(keycloakAdminService.listRoles())
            .thenReturn(
                List.of(
                    new KeycloakAdminService.ManagedKeycloakRole(
                        AuthoritiesConstants.MEDICO,
                        "Acceso médico",
                        Set.of("admin.users.manage"),
                        Set.of()
                    )
                )
            );
        when(roleDefinitionRepository.findAll()).thenAnswer(invocation -> List.copyOf(storedRoles.values()));
        when(roleDefinitionRepository.findAllByRoleNameIn(anyCollection())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Set<String> names = Set.copyOf((java.util.Collection<String>) invocation.getArgument(0));
            return storedRoles.values().stream().filter(role -> names.contains(role.getRoleName())).collect(Collectors.toList());
        });
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Iterable<RoleDefinition> deleted = invocation.getArgument(0);
            deleted.forEach(role -> storedRoles.remove(role.getRoleName()));
            return null;
        }).when(roleDefinitionRepository).deleteAll(anyCollection());
        when(roleDefinitionRepository.save(any(RoleDefinition.class))).thenAnswer(invocation -> {
            RoleDefinition saved = invocation.getArgument(0);
            storedRoles.put(saved.getRoleName(), saved);
            return saved;
        });
        when(authorityRepository.findAll()).thenAnswer(invocation -> List.copyOf(storedAuthorities.values()));
        when(authorityRepository.existsById(anyString())).thenAnswer(invocation -> storedAuthorities.containsKey(invocation.getArgument(0)));
        when(authorityRepository.save(any(Authority.class))).thenAnswer(invocation -> {
            Authority saved = invocation.getArgument(0);
            storedAuthorities.put(saved.getName(), saved);
            return saved;
        });
        doAnswer(invocation -> {
            storedAuthorities.remove(invocation.getArgument(0));
            return null;
        }).when(authorityRepository).deleteById(anyString());
        when(userRepository.findAll()).thenReturn(List.of(updatedUser.get()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            updatedUser.set(saved);
            return saved;
        });

        List<RoleDefinitionDTO> roles = roleAdministrationService.getAllRoles();

        assertThat(roles).extracting(RoleDefinitionDTO::roleName).containsExactly(AuthoritiesConstants.MEDICO);
        assertThat(storedRoles).doesNotContainKey("ROLE_OBSOLETO");
        assertThat(storedAuthorities).doesNotContainKey("ROLE_OBSOLETO");
        assertThat(updatedUser.get().getAuthorities()).extracting(Authority::getName).doesNotContain("ROLE_OBSOLETO");
        verify(authorityRepository).deleteById("ROLE_OBSOLETO");
    }

    @Test
    void getAllRolesKeepsCanonicalPermissionsForAdminSystemRole() {
        Map<String, RoleDefinition> storedRoles = new HashMap<>();

        when(keycloakAdminService.listRoles())
            .thenReturn(
                List.of(
                    new KeycloakAdminService.ManagedKeycloakRole(
                        AuthoritiesConstants.ADMIN,
                        "Rol de Keycloak",
                        Set.of(),
                        Set.of(AuthoritiesConstants.USER)
                    )
                )
            );
        when(roleDefinitionRepository.findAll()).thenReturn(List.copyOf(storedRoles.values()));
        when(roleDefinitionRepository.findAllByRoleNameIn(anyCollection())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Set<String> names = Set.copyOf((java.util.Collection<String>) invocation.getArgument(0));
            return storedRoles.values().stream().filter(role -> names.contains(role.getRoleName())).collect(Collectors.toList());
        });
        when(roleDefinitionRepository.save(any(RoleDefinition.class))).thenAnswer(invocation -> {
            RoleDefinition saved = invocation.getArgument(0);
            storedRoles.put(saved.getRoleName(), saved);
            return saved;
        });
        when(authorityRepository.findAll()).thenReturn(List.of());
        when(authorityRepository.existsById(anyString())).thenReturn(false);
        when(authorityRepository.save(any(Authority.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<RoleDefinitionDTO> roles = roleAdministrationService.getAllRoles();

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).roleName()).isEqualTo(AuthoritiesConstants.ADMIN);
        assertThat(roles.get(0).permissions()).containsExactlyInAnyOrderElementsOf(AppPermissionCatalog.allCodes());
        assertThat(roles.get(0).compositeRoles()).isEmpty();
        assertThat(roles.get(0).description()).isEqualTo("Administrador del sistema");
    }

    private static RoleDefinition roleDefinition(
        String roleName,
        String description,
        boolean systemRole,
        Set<String> permissions,
        Set<String> compositeRoles
    ) {
        RoleDefinition roleDefinition = new RoleDefinition();
        roleDefinition.setRoleName(roleName);
        roleDefinition.setDescription(description);
        roleDefinition.setSystemRole(systemRole);
        roleDefinition.setPermissions(permissions);
        roleDefinition.setCompositeRoles(compositeRoles);
        return roleDefinition;
    }

    private static RoleDefinition anyUpdatedAdminRole() {
        return org.mockito.ArgumentMatchers.argThat((ArgumentMatcher<RoleDefinition>) roleDefinition ->
            roleDefinition != null &&
                AuthoritiesConstants.ADMIN.equals(roleDefinition.getRoleName()) &&
                roleDefinition.isSystemRole() &&
                roleDefinition.getCompositeRoles().isEmpty() &&
                roleDefinition.getPermissions().containsAll(AppPermissionCatalog.allCodes())
        );
    }

    private static Authority authority(String name) {
        Authority authority = new Authority();
        authority.setName(name);
        return authority;
    }
}
