package ni.edu.mney.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class KeycloakAdminServiceTest {

    private static final String ISSUER_URI = "http://localhost/realms/control-expedientes";

    private KeycloakAdminService keycloakAdminService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        keycloakAdminService = new KeycloakAdminService();
        ReflectionTestUtils.setField(keycloakAdminService, "issuerUri", ISSUER_URI);
        ReflectionTestUtils.setField(keycloakAdminService, "adminClientId", "admin-cli");
        ReflectionTestUtils.setField(keycloakAdminService, "adminUsername", "admin");
        ReflectionTestUtils.setField(keycloakAdminService, "adminPassword", "admin");

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(keycloakAdminService, "restTemplate");
        server = MockRestServiceServer.bindTo(restTemplate).build();
        server
            .expect(ExpectedCount.manyTimes(), requestTo(ISSUER_URI + "/protocol/openid-connect/token"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"access_token\":\"test-token\"}", MediaType.APPLICATION_JSON));
    }

    @Test
    void listRolesRequestsFullRepresentationAndPreservesIndividualPermissions() {
        server
            .expect(requestTo("http://localhost/admin/realms/control-expedientes/roles?briefRepresentation=false"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                    """
                    [
                      {
                        "name": "ROLE_EXPORTADOR",
                        "description": "Puede exportar respaldos",
                        "clientRole": false,
                        "attributes": {
                          "permissions": ["admin.database.export"]
                        }
                      }
                    ]
                    """,
                    MediaType.APPLICATION_JSON
                )
            );
        server
            .expect(requestTo("http://localhost/admin/realms/control-expedientes/roles/ROLE_EXPORTADOR/composites"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<KeycloakAdminService.ManagedKeycloakRole> roles = keycloakAdminService.listRoles();

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).roleName()).isEqualTo("ROLE_EXPORTADOR");
        assertThat(roles.get(0).permissions()).containsExactly("admin.database.export");
        assertThat(roles.get(0).compositeRoles()).isEmpty();
        server.verify();
    }
}
