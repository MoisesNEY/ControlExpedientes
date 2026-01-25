package ni.edu.mney.domain;

import static ni.edu.mney.domain.AuditoriaAccionesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuditoriaAccionesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuditoriaAcciones.class);
        AuditoriaAcciones auditoriaAcciones1 = getAuditoriaAccionesSample1();
        AuditoriaAcciones auditoriaAcciones2 = new AuditoriaAcciones();
        assertThat(auditoriaAcciones1).isNotEqualTo(auditoriaAcciones2);

        auditoriaAcciones2.setId(auditoriaAcciones1.getId());
        assertThat(auditoriaAcciones1).isEqualTo(auditoriaAcciones2);

        auditoriaAcciones2 = getAuditoriaAccionesSample2();
        assertThat(auditoriaAcciones1).isNotEqualTo(auditoriaAcciones2);
    }
}
