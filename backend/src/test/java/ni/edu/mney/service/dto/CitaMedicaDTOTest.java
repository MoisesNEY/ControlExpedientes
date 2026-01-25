package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CitaMedicaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CitaMedicaDTO.class);
        CitaMedicaDTO citaMedicaDTO1 = new CitaMedicaDTO();
        citaMedicaDTO1.setId(1L);
        CitaMedicaDTO citaMedicaDTO2 = new CitaMedicaDTO();
        assertThat(citaMedicaDTO1).isNotEqualTo(citaMedicaDTO2);
        citaMedicaDTO2.setId(citaMedicaDTO1.getId());
        assertThat(citaMedicaDTO1).isEqualTo(citaMedicaDTO2);
        citaMedicaDTO2.setId(2L);
        assertThat(citaMedicaDTO1).isNotEqualTo(citaMedicaDTO2);
        citaMedicaDTO1.setId(null);
        assertThat(citaMedicaDTO1).isNotEqualTo(citaMedicaDTO2);
    }
}
