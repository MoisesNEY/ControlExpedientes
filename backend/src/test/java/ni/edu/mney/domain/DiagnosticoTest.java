package ni.edu.mney.domain;

import static ni.edu.mney.domain.ConsultaMedicaTestSamples.*;
import static ni.edu.mney.domain.DiagnosticoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DiagnosticoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Diagnostico.class);
        Diagnostico diagnostico1 = getDiagnosticoSample1();
        Diagnostico diagnostico2 = new Diagnostico();
        assertThat(diagnostico1).isNotEqualTo(diagnostico2);

        diagnostico2.setId(diagnostico1.getId());
        assertThat(diagnostico1).isEqualTo(diagnostico2);

        diagnostico2 = getDiagnosticoSample2();
        assertThat(diagnostico1).isNotEqualTo(diagnostico2);
    }

    @Test
    void consultaTest() {
        Diagnostico diagnostico = getDiagnosticoRandomSampleGenerator();
        ConsultaMedica consultaMedicaBack = getConsultaMedicaRandomSampleGenerator();

        diagnostico.setConsulta(consultaMedicaBack);
        assertThat(diagnostico.getConsulta()).isEqualTo(consultaMedicaBack);

        diagnostico.consulta(null);
        assertThat(diagnostico.getConsulta()).isNull();
    }
}
