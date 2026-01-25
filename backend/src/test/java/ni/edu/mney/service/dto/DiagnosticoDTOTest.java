package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DiagnosticoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DiagnosticoDTO.class);
        DiagnosticoDTO diagnosticoDTO1 = new DiagnosticoDTO();
        diagnosticoDTO1.setId(1L);
        DiagnosticoDTO diagnosticoDTO2 = new DiagnosticoDTO();
        assertThat(diagnosticoDTO1).isNotEqualTo(diagnosticoDTO2);
        diagnosticoDTO2.setId(diagnosticoDTO1.getId());
        assertThat(diagnosticoDTO1).isEqualTo(diagnosticoDTO2);
        diagnosticoDTO2.setId(2L);
        assertThat(diagnosticoDTO1).isNotEqualTo(diagnosticoDTO2);
        diagnosticoDTO1.setId(null);
        assertThat(diagnosticoDTO1).isNotEqualTo(diagnosticoDTO2);
    }
}
