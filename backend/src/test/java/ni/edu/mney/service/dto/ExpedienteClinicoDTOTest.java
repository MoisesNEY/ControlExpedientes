package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExpedienteClinicoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExpedienteClinicoDTO.class);
        ExpedienteClinicoDTO expedienteClinicoDTO1 = new ExpedienteClinicoDTO();
        expedienteClinicoDTO1.setId(1L);
        ExpedienteClinicoDTO expedienteClinicoDTO2 = new ExpedienteClinicoDTO();
        assertThat(expedienteClinicoDTO1).isNotEqualTo(expedienteClinicoDTO2);
        expedienteClinicoDTO2.setId(expedienteClinicoDTO1.getId());
        assertThat(expedienteClinicoDTO1).isEqualTo(expedienteClinicoDTO2);
        expedienteClinicoDTO2.setId(2L);
        assertThat(expedienteClinicoDTO1).isNotEqualTo(expedienteClinicoDTO2);
        expedienteClinicoDTO1.setId(null);
        assertThat(expedienteClinicoDTO1).isNotEqualTo(expedienteClinicoDTO2);
    }
}
