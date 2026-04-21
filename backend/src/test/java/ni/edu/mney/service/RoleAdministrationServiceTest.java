package ni.edu.mney.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ni.edu.mney.domain.RoleDefinition;
import ni.edu.mney.repository.AuthorityRepository;
import ni.edu.mney.repository.RoleDefinitionRepository;
import ni.edu.mney.security.AppPermissionCatalog;
import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.security.KeycloakAdminService;
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
}
