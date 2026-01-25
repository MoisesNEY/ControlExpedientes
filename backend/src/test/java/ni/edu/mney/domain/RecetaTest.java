package ni.edu.mney.domain;

import static ni.edu.mney.domain.ConsultaMedicaTestSamples.*;
import static ni.edu.mney.domain.MedicamentoTestSamples.*;
import static ni.edu.mney.domain.RecetaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RecetaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Receta.class);
        Receta receta1 = getRecetaSample1();
        Receta receta2 = new Receta();
        assertThat(receta1).isNotEqualTo(receta2);

        receta2.setId(receta1.getId());
        assertThat(receta1).isEqualTo(receta2);

        receta2 = getRecetaSample2();
        assertThat(receta1).isNotEqualTo(receta2);
    }

    @Test
    void medicamentoTest() {
        Receta receta = getRecetaRandomSampleGenerator();
        Medicamento medicamentoBack = getMedicamentoRandomSampleGenerator();

        receta.setMedicamento(medicamentoBack);
        assertThat(receta.getMedicamento()).isEqualTo(medicamentoBack);

        receta.medicamento(null);
        assertThat(receta.getMedicamento()).isNull();
    }

    @Test
    void consultaTest() {
        Receta receta = getRecetaRandomSampleGenerator();
        ConsultaMedica consultaMedicaBack = getConsultaMedicaRandomSampleGenerator();

        receta.setConsulta(consultaMedicaBack);
        assertThat(receta.getConsulta()).isEqualTo(consultaMedicaBack);

        receta.consulta(null);
        assertThat(receta.getConsulta()).isNull();
    }
}
