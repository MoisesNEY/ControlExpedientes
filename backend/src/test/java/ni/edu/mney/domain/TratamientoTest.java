package ni.edu.mney.domain;

import static ni.edu.mney.domain.ConsultaMedicaTestSamples.*;
import static ni.edu.mney.domain.TratamientoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TratamientoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Tratamiento.class);
        Tratamiento tratamiento1 = getTratamientoSample1();
        Tratamiento tratamiento2 = new Tratamiento();
        assertThat(tratamiento1).isNotEqualTo(tratamiento2);

        tratamiento2.setId(tratamiento1.getId());
        assertThat(tratamiento1).isEqualTo(tratamiento2);

        tratamiento2 = getTratamientoSample2();
        assertThat(tratamiento1).isNotEqualTo(tratamiento2);
    }

    @Test
    void consultaTest() {
        Tratamiento tratamiento = getTratamientoRandomSampleGenerator();
        ConsultaMedica consultaMedicaBack = getConsultaMedicaRandomSampleGenerator();

        tratamiento.setConsulta(consultaMedicaBack);
        assertThat(tratamiento.getConsulta()).isEqualTo(consultaMedicaBack);

        tratamiento.consulta(null);
        assertThat(tratamiento.getConsulta()).isNull();
    }
}
