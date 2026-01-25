package ni.edu.mney.domain;

import static ni.edu.mney.domain.CitaMedicaTestSamples.*;
import static ni.edu.mney.domain.PacienteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CitaMedicaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CitaMedica.class);
        CitaMedica citaMedica1 = getCitaMedicaSample1();
        CitaMedica citaMedica2 = new CitaMedica();
        assertThat(citaMedica1).isNotEqualTo(citaMedica2);

        citaMedica2.setId(citaMedica1.getId());
        assertThat(citaMedica1).isEqualTo(citaMedica2);

        citaMedica2 = getCitaMedicaSample2();
        assertThat(citaMedica1).isNotEqualTo(citaMedica2);
    }

    @Test
    void pacienteTest() {
        CitaMedica citaMedica = getCitaMedicaRandomSampleGenerator();
        Paciente pacienteBack = getPacienteRandomSampleGenerator();

        citaMedica.setPaciente(pacienteBack);
        assertThat(citaMedica.getPaciente()).isEqualTo(pacienteBack);

        citaMedica.paciente(null);
        assertThat(citaMedica.getPaciente()).isNull();
    }
}
