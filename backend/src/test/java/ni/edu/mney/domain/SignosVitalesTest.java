package ni.edu.mney.domain;

import static ni.edu.mney.domain.ConsultaMedicaTestSamples.*;
import static ni.edu.mney.domain.SignosVitalesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SignosVitalesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SignosVitales.class);
        SignosVitales signosVitales1 = getSignosVitalesSample1();
        SignosVitales signosVitales2 = new SignosVitales();
        assertThat(signosVitales1).isNotEqualTo(signosVitales2);

        signosVitales2.setId(signosVitales1.getId());
        assertThat(signosVitales1).isEqualTo(signosVitales2);

        signosVitales2 = getSignosVitalesSample2();
        assertThat(signosVitales1).isNotEqualTo(signosVitales2);
    }

    @Test
    void consultaTest() {
        SignosVitales signosVitales = getSignosVitalesRandomSampleGenerator();
        ConsultaMedica consultaMedicaBack = getConsultaMedicaRandomSampleGenerator();

        signosVitales.setConsulta(consultaMedicaBack);
        assertThat(signosVitales.getConsulta()).isEqualTo(consultaMedicaBack);

        signosVitales.consulta(null);
        assertThat(signosVitales.getConsulta()).isNull();
    }
}
