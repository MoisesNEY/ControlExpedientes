package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HistorialClinicoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(HistorialClinicoDTO.class);
        HistorialClinicoDTO historialClinicoDTO1 = new HistorialClinicoDTO();
        historialClinicoDTO1.setId(1L);
        HistorialClinicoDTO historialClinicoDTO2 = new HistorialClinicoDTO();
        assertThat(historialClinicoDTO1).isNotEqualTo(historialClinicoDTO2);
        historialClinicoDTO2.setId(historialClinicoDTO1.getId());
        assertThat(historialClinicoDTO1).isEqualTo(historialClinicoDTO2);
        historialClinicoDTO2.setId(2L);
        assertThat(historialClinicoDTO1).isNotEqualTo(historialClinicoDTO2);
        historialClinicoDTO1.setId(null);
        assertThat(historialClinicoDTO1).isNotEqualTo(historialClinicoDTO2);
    }
}
