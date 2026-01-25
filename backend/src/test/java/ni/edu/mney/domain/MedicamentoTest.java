package ni.edu.mney.domain;

import static ni.edu.mney.domain.MedicamentoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MedicamentoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Medicamento.class);
        Medicamento medicamento1 = getMedicamentoSample1();
        Medicamento medicamento2 = new Medicamento();
        assertThat(medicamento1).isNotEqualTo(medicamento2);

        medicamento2.setId(medicamento1.getId());
        assertThat(medicamento1).isEqualTo(medicamento2);

        medicamento2 = getMedicamentoSample2();
        assertThat(medicamento1).isNotEqualTo(medicamento2);
    }
}
