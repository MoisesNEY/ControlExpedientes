package ni.edu.mney.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import ni.edu.mney.repository.AuthorityRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.security.KeycloakAdminService;
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
    void createUserRejectsTemporaryPasswords() {
        ManagedUserUpsertDTO request = new ManagedUserUpsertDTO(
            "pepito",
            "Pepito",
            "Perez",
            "pepito@example.com",
            true,
            List.of("ROLE_MEDICO"),
            "secreta",
            true,
            List.of()
        );

        assertThatThrownBy(() -> userAdministrationService.createUser(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("contraseñas temporales");

        verifyNoInteractions(keycloakAdminService);
    }

    @Test
    void createUserRejectsRequiredActions() {
        ManagedUserUpsertDTO request = new ManagedUserUpsertDTO(
            "pepito",
            "Pepito",
            "Perez",
            "pepito@example.com",
            true,
            List.of("ROLE_MEDICO"),
            "secreta",
            false,
            List.of("UPDATE_PASSWORD")
        );

        assertThatThrownBy(() -> userAdministrationService.createUser(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("acciones obligatorias");

        verifyNoInteractions(keycloakAdminService);
    }
}
