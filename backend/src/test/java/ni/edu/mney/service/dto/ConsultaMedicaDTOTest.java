package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConsultaMedicaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConsultaMedicaDTO.class);
        ConsultaMedicaDTO consultaMedicaDTO1 = new ConsultaMedicaDTO();
        consultaMedicaDTO1.setId(1L);
        ConsultaMedicaDTO consultaMedicaDTO2 = new ConsultaMedicaDTO();
        assertThat(consultaMedicaDTO1).isNotEqualTo(consultaMedicaDTO2);
        consultaMedicaDTO2.setId(consultaMedicaDTO1.getId());
        assertThat(consultaMedicaDTO1).isEqualTo(consultaMedicaDTO2);
        consultaMedicaDTO2.setId(2L);
        assertThat(consultaMedicaDTO1).isNotEqualTo(consultaMedicaDTO2);
        consultaMedicaDTO1.setId(null);
        assertThat(consultaMedicaDTO1).isNotEqualTo(consultaMedicaDTO2);
    }
}
