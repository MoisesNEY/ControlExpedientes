package ni.edu.mney.domain;

import static ni.edu.mney.domain.ExpedienteClinicoTestSamples.*;
import static ni.edu.mney.domain.HistorialClinicoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HistorialClinicoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(HistorialClinico.class);
        HistorialClinico historialClinico1 = getHistorialClinicoSample1();
        HistorialClinico historialClinico2 = new HistorialClinico();
        assertThat(historialClinico1).isNotEqualTo(historialClinico2);

        historialClinico2.setId(historialClinico1.getId());
        assertThat(historialClinico1).isEqualTo(historialClinico2);

        historialClinico2 = getHistorialClinicoSample2();
        assertThat(historialClinico1).isNotEqualTo(historialClinico2);
    }

    @Test
    void expedienteTest() {
        HistorialClinico historialClinico = getHistorialClinicoRandomSampleGenerator();
        ExpedienteClinico expedienteClinicoBack = getExpedienteClinicoRandomSampleGenerator();

        historialClinico.setExpediente(expedienteClinicoBack);
        assertThat(historialClinico.getExpediente()).isEqualTo(expedienteClinicoBack);

        historialClinico.expediente(null);
        assertThat(historialClinico.getExpediente()).isNull();
    }
}
