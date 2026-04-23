package ni.edu.mney.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import ni.edu.mney.domain.Authority;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.AuthorityRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.security.KeycloakAdminService;
import ni.edu.mney.service.dto.ManagedUserDTO;
import ni.edu.mney.service.dto.ManagedUserUpsertDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdministrationServiceTest {

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private UserAdministrationService userAdministrationService;

    @Test
    void createUserAllowsTemporaryPasswordsAndRequiredActions() {
        ManagedUserUpsertDTO request = new ManagedUserUpsertDTO(
            "pepito",
            "Pepito",
            "Perez",
            "pepito@example.com",
            true,
            List.of("ROLE_MEDICO"),
            "secreta",
            true,
            List.of("UPDATE_PASSWORD")
        );

        when(keycloakAdminService.createUser(anyString(), any(), any(), any(), anyBoolean(), any(), any(), anyBoolean(), any()))
            .thenReturn(new KeycloakAdminService.ManagedKeycloakUser("user-1", "pepito", "Pepito", "Perez", "pepito@example.com", true, List.of("ROLE_MEDICO"), List.of("UPDATE_PASSWORD")));
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authorityRepository.findById("ROLE_MEDICO")).thenReturn(Optional.of(authority("ROLE_MEDICO")));

        ManagedUserDTO created = userAdministrationService.createUser(request);

        assertThat(created.requiredActions()).containsExactly("UPDATE_PASSWORD");
        verify(keycloakAdminService).createUser("pepito", "Pepito", "Perez", "pepito@example.com", true, List.of("ROLE_MEDICO"), "secreta", true, List.of("UPDATE_PASSWORD"));
    }

    @Test
    void getAllUsersRemovesLocalUsersMissingInKeycloak() {
        User staleUser = new User();
        staleUser.setId("stale-user");
        staleUser.setLogin("stale");
        staleUser.setAuthorities(Set.of(authority("ROLE_MEDICO")));

        when(keycloakAdminService.listUsers())
            .thenReturn(List.of(new KeycloakAdminService.ManagedKeycloakUser("user-1", "pepito", "Pepito", "Perez", "pepito@example.com", true, List.of("ROLE_MEDICO"), List.of())));
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of(staleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authorityRepository.findById("ROLE_MEDICO")).thenReturn(Optional.of(authority("ROLE_MEDICO")));

        List<ManagedUserDTO> users = userAdministrationService.getAllUsers();

        assertThat(users).extracting(ManagedUserDTO::login).containsExactly("pepito");
        verify(userRepository).delete(staleUser);
    }

    @Test
    void deleteUserRemovesRemoteAndLocalCopies() {
        User existingUser = new User();
        existingUser.setId("user-1");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));

        userAdministrationService.deleteUser("user-1");

        verify(keycloakAdminService).deleteUser("user-1");
        verify(userRepository).delete(existingUser);
    }

    @Test
    void deleteUserSkipsLocalDeleteWhenUserIsNotSynced() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        userAdministrationService.deleteUser("missing");

        verify(keycloakAdminService).deleteUser("missing");
        verify(userRepository, never()).delete(any(User.class));
    }

    private static Authority authority(String name) {
        Authority authority = new Authority();
        authority.setName(name);
        return authority;
    }
}
