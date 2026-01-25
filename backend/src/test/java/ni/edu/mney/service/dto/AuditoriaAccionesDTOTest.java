package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuditoriaAccionesDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuditoriaAccionesDTO.class);
        AuditoriaAccionesDTO auditoriaAccionesDTO1 = new AuditoriaAccionesDTO();
        auditoriaAccionesDTO1.setId(1L);
        AuditoriaAccionesDTO auditoriaAccionesDTO2 = new AuditoriaAccionesDTO();
        assertThat(auditoriaAccionesDTO1).isNotEqualTo(auditoriaAccionesDTO2);
        auditoriaAccionesDTO2.setId(auditoriaAccionesDTO1.getId());
        assertThat(auditoriaAccionesDTO1).isEqualTo(auditoriaAccionesDTO2);
        auditoriaAccionesDTO2.setId(2L);
        assertThat(auditoriaAccionesDTO1).isNotEqualTo(auditoriaAccionesDTO2);
        auditoriaAccionesDTO1.setId(null);
        assertThat(auditoriaAccionesDTO1).isNotEqualTo(auditoriaAccionesDTO2);
    }
}
