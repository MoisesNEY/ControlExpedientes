package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TratamientoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TratamientoDTO.class);
        TratamientoDTO tratamientoDTO1 = new TratamientoDTO();
        tratamientoDTO1.setId(1L);
        TratamientoDTO tratamientoDTO2 = new TratamientoDTO();
        assertThat(tratamientoDTO1).isNotEqualTo(tratamientoDTO2);
        tratamientoDTO2.setId(tratamientoDTO1.getId());
        assertThat(tratamientoDTO1).isEqualTo(tratamientoDTO2);
        tratamientoDTO2.setId(2L);
        assertThat(tratamientoDTO1).isNotEqualTo(tratamientoDTO2);
        tratamientoDTO1.setId(null);
        assertThat(tratamientoDTO1).isNotEqualTo(tratamientoDTO2);
    }
}
