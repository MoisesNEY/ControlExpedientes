package ni.edu.mney.domain;

import static ni.edu.mney.domain.ConsultaMedicaTestSamples.*;
import static ni.edu.mney.domain.ExpedienteClinicoTestSamples.*;
import static ni.edu.mney.domain.HistorialClinicoTestSamples.*;
import static ni.edu.mney.domain.PacienteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExpedienteClinicoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExpedienteClinico.class);
        ExpedienteClinico expedienteClinico1 = getExpedienteClinicoSample1();
        ExpedienteClinico expedienteClinico2 = new ExpedienteClinico();
        assertThat(expedienteClinico1).isNotEqualTo(expedienteClinico2);

        expedienteClinico2.setId(expedienteClinico1.getId());
        assertThat(expedienteClinico1).isEqualTo(expedienteClinico2);

        expedienteClinico2 = getExpedienteClinicoSample2();
        assertThat(expedienteClinico1).isNotEqualTo(expedienteClinico2);
    }

    @Test
    void consultaTest() {
        ExpedienteClinico expedienteClinico = getExpedienteClinicoRandomSampleGenerator();
        ConsultaMedica consultaMedicaBack = getConsultaMedicaRandomSampleGenerator();

        expedienteClinico.addConsulta(consultaMedicaBack);
        assertThat(expedienteClinico.getConsultas()).containsOnly(consultaMedicaBack);
        assertThat(consultaMedicaBack.getExpediente()).isEqualTo(expedienteClinico);

        expedienteClinico.removeConsulta(consultaMedicaBack);
        assertThat(expedienteClinico.getConsultas()).doesNotContain(consultaMedicaBack);
        assertThat(consultaMedicaBack.getExpediente()).isNull();

        expedienteClinico.consultas(new HashSet<>(Set.of(consultaMedicaBack)));
        assertThat(expedienteClinico.getConsultas()).containsOnly(consultaMedicaBack);
        assertThat(consultaMedicaBack.getExpediente()).isEqualTo(expedienteClinico);

        expedienteClinico.setConsultas(new HashSet<>());
        assertThat(expedienteClinico.getConsultas()).doesNotContain(consultaMedicaBack);
        assertThat(consultaMedicaBack.getExpediente()).isNull();
    }

    @Test
    void historialTest() {
        ExpedienteClinico expedienteClinico = getExpedienteClinicoRandomSampleGenerator();
        HistorialClinico historialClinicoBack = getHistorialClinicoRandomSampleGenerator();

        expedienteClinico.addHistorial(historialClinicoBack);
        assertThat(expedienteClinico.getHistorials()).containsOnly(historialClinicoBack);
        assertThat(historialClinicoBack.getExpediente()).isEqualTo(expedienteClinico);

        expedienteClinico.removeHistorial(historialClinicoBack);
        assertThat(expedienteClinico.getHistorials()).doesNotContain(historialClinicoBack);
        assertThat(historialClinicoBack.getExpediente()).isNull();

        expedienteClinico.historials(new HashSet<>(Set.of(historialClinicoBack)));
        assertThat(expedienteClinico.getHistorials()).containsOnly(historialClinicoBack);
        assertThat(historialClinicoBack.getExpediente()).isEqualTo(expedienteClinico);

        expedienteClinico.setHistorials(new HashSet<>());
        assertThat(expedienteClinico.getHistorials()).doesNotContain(historialClinicoBack);
        assertThat(historialClinicoBack.getExpediente()).isNull();
    }

    @Test
    void pacienteTest() {
        ExpedienteClinico expedienteClinico = getExpedienteClinicoRandomSampleGenerator();
        Paciente pacienteBack = getPacienteRandomSampleGenerator();

        expedienteClinico.setPaciente(pacienteBack);
        assertThat(expedienteClinico.getPaciente()).isEqualTo(pacienteBack);
        assertThat(pacienteBack.getExpediente()).isEqualTo(expedienteClinico);

        expedienteClinico.paciente(null);
        assertThat(expedienteClinico.getPaciente()).isNull();
        assertThat(pacienteBack.getExpediente()).isNull();
    }
}
