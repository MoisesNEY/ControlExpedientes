package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SignosVitalesDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SignosVitalesDTO.class);
        SignosVitalesDTO signosVitalesDTO1 = new SignosVitalesDTO();
        signosVitalesDTO1.setId(1L);
        SignosVitalesDTO signosVitalesDTO2 = new SignosVitalesDTO();
        assertThat(signosVitalesDTO1).isNotEqualTo(signosVitalesDTO2);
        signosVitalesDTO2.setId(signosVitalesDTO1.getId());
        assertThat(signosVitalesDTO1).isEqualTo(signosVitalesDTO2);
        signosVitalesDTO2.setId(2L);
        assertThat(signosVitalesDTO1).isNotEqualTo(signosVitalesDTO2);
        signosVitalesDTO1.setId(null);
        assertThat(signosVitalesDTO1).isNotEqualTo(signosVitalesDTO2);
    }
}
