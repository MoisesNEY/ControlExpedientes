package ni.edu.mney.domain;

import static ni.edu.mney.domain.ConsultaMedicaTestSamples.*;
import static ni.edu.mney.domain.DiagnosticoTestSamples.*;
import static ni.edu.mney.domain.ExpedienteClinicoTestSamples.*;
import static ni.edu.mney.domain.RecetaTestSamples.*;
import static ni.edu.mney.domain.SignosVitalesTestSamples.*;
import static ni.edu.mney.domain.TratamientoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConsultaMedicaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConsultaMedica.class);
        ConsultaMedica consultaMedica1 = getConsultaMedicaSample1();
        ConsultaMedica consultaMedica2 = new ConsultaMedica();
        assertThat(consultaMedica1).isNotEqualTo(consultaMedica2);

        consultaMedica2.setId(consultaMedica1.getId());
        assertThat(consultaMedica1).isEqualTo(consultaMedica2);

        consultaMedica2 = getConsultaMedicaSample2();
        assertThat(consultaMedica1).isNotEqualTo(consultaMedica2);
    }

    @Test
    void diagnosticoTest() {
        ConsultaMedica consultaMedica = getConsultaMedicaRandomSampleGenerator();
        Diagnostico diagnosticoBack = getDiagnosticoRandomSampleGenerator();

        consultaMedica.addDiagnostico(diagnosticoBack);
        assertThat(consultaMedica.getDiagnosticos()).containsOnly(diagnosticoBack);
        assertThat(diagnosticoBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.removeDiagnostico(diagnosticoBack);
        assertThat(consultaMedica.getDiagnosticos()).doesNotContain(diagnosticoBack);
        assertThat(diagnosticoBack.getConsulta()).isNull();

        consultaMedica.diagnosticos(new HashSet<>(Set.of(diagnosticoBack)));
        assertThat(consultaMedica.getDiagnosticos()).containsOnly(diagnosticoBack);
        assertThat(diagnosticoBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.setDiagnosticos(new HashSet<>());
        assertThat(consultaMedica.getDiagnosticos()).doesNotContain(diagnosticoBack);
        assertThat(diagnosticoBack.getConsulta()).isNull();
    }

    @Test
    void tratamientoTest() {
        ConsultaMedica consultaMedica = getConsultaMedicaRandomSampleGenerator();
        Tratamiento tratamientoBack = getTratamientoRandomSampleGenerator();

        consultaMedica.addTratamiento(tratamientoBack);
        assertThat(consultaMedica.getTratamientos()).containsOnly(tratamientoBack);
        assertThat(tratamientoBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.removeTratamiento(tratamientoBack);
        assertThat(consultaMedica.getTratamientos()).doesNotContain(tratamientoBack);
        assertThat(tratamientoBack.getConsulta()).isNull();

        consultaMedica.tratamientos(new HashSet<>(Set.of(tratamientoBack)));
        assertThat(consultaMedica.getTratamientos()).containsOnly(tratamientoBack);
        assertThat(tratamientoBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.setTratamientos(new HashSet<>());
        assertThat(consultaMedica.getTratamientos()).doesNotContain(tratamientoBack);
        assertThat(tratamientoBack.getConsulta()).isNull();
    }

    @Test
    void signosVitalesTest() {
        ConsultaMedica consultaMedica = getConsultaMedicaRandomSampleGenerator();
        SignosVitales signosVitalesBack = getSignosVitalesRandomSampleGenerator();

        consultaMedica.addSignosVitales(signosVitalesBack);
        assertThat(consultaMedica.getSignosVitales()).containsOnly(signosVitalesBack);
        assertThat(signosVitalesBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.removeSignosVitales(signosVitalesBack);
        assertThat(consultaMedica.getSignosVitales()).doesNotContain(signosVitalesBack);
        assertThat(signosVitalesBack.getConsulta()).isNull();

        consultaMedica.signosVitales(new HashSet<>(Set.of(signosVitalesBack)));
        assertThat(consultaMedica.getSignosVitales()).containsOnly(signosVitalesBack);
        assertThat(signosVitalesBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.setSignosVitales(new HashSet<>());
        assertThat(consultaMedica.getSignosVitales()).doesNotContain(signosVitalesBack);
        assertThat(signosVitalesBack.getConsulta()).isNull();
    }

    @Test
    void recetaTest() {
        ConsultaMedica consultaMedica = getConsultaMedicaRandomSampleGenerator();
        Receta recetaBack = getRecetaRandomSampleGenerator();

        consultaMedica.addReceta(recetaBack);
        assertThat(consultaMedica.getRecetas()).containsOnly(recetaBack);
        assertThat(recetaBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.removeReceta(recetaBack);
        assertThat(consultaMedica.getRecetas()).doesNotContain(recetaBack);
        assertThat(recetaBack.getConsulta()).isNull();

        consultaMedica.recetas(new HashSet<>(Set.of(recetaBack)));
        assertThat(consultaMedica.getRecetas()).containsOnly(recetaBack);
        assertThat(recetaBack.getConsulta()).isEqualTo(consultaMedica);

        consultaMedica.setRecetas(new HashSet<>());
        assertThat(consultaMedica.getRecetas()).doesNotContain(recetaBack);
        assertThat(recetaBack.getConsulta()).isNull();
    }

    @Test
    void expedienteTest() {
        ConsultaMedica consultaMedica = getConsultaMedicaRandomSampleGenerator();
        ExpedienteClinico expedienteClinicoBack = getExpedienteClinicoRandomSampleGenerator();

        consultaMedica.setExpediente(expedienteClinicoBack);
        assertThat(consultaMedica.getExpediente()).isEqualTo(expedienteClinicoBack);

        consultaMedica.expediente(null);
        assertThat(consultaMedica.getExpediente()).isNull();
    }
}
