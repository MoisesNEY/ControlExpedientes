package ni.edu.mney.domain;

import static ni.edu.mney.domain.CitaMedicaTestSamples.*;
import static ni.edu.mney.domain.ExpedienteClinicoTestSamples.*;
import static ni.edu.mney.domain.PacienteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PacienteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Paciente.class);
        Paciente paciente1 = getPacienteSample1();
        Paciente paciente2 = new Paciente();
        assertThat(paciente1).isNotEqualTo(paciente2);

        paciente2.setId(paciente1.getId());
        assertThat(paciente1).isEqualTo(paciente2);

        paciente2 = getPacienteSample2();
        assertThat(paciente1).isNotEqualTo(paciente2);
    }

    @Test
    void expedienteTest() {
        Paciente paciente = getPacienteRandomSampleGenerator();
        ExpedienteClinico expedienteClinicoBack = getExpedienteClinicoRandomSampleGenerator();

        paciente.setExpediente(expedienteClinicoBack);
        assertThat(paciente.getExpediente()).isEqualTo(expedienteClinicoBack);

        paciente.expediente(null);
        assertThat(paciente.getExpediente()).isNull();
    }

    @Test
    void citaTest() {
        Paciente paciente = getPacienteRandomSampleGenerator();
        CitaMedica citaMedicaBack = getCitaMedicaRandomSampleGenerator();

        paciente.addCita(citaMedicaBack);
        assertThat(paciente.getCitas()).containsOnly(citaMedicaBack);
        assertThat(citaMedicaBack.getPaciente()).isEqualTo(paciente);

        paciente.removeCita(citaMedicaBack);
        assertThat(paciente.getCitas()).doesNotContain(citaMedicaBack);
        assertThat(citaMedicaBack.getPaciente()).isNull();

        paciente.citas(new HashSet<>(Set.of(citaMedicaBack)));
        assertThat(paciente.getCitas()).containsOnly(citaMedicaBack);
        assertThat(citaMedicaBack.getPaciente()).isEqualTo(paciente);

        paciente.setCitas(new HashSet<>());
        assertThat(paciente.getCitas()).doesNotContain(citaMedicaBack);
        assertThat(citaMedicaBack.getPaciente()).isNull();
    }
}
