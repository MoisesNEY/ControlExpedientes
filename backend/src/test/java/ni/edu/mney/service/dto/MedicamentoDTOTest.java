package ni.edu.mney.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ni.edu.mney.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MedicamentoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedicamentoDTO.class);
        MedicamentoDTO medicamentoDTO1 = new MedicamentoDTO();
        medicamentoDTO1.setId(1L);
        MedicamentoDTO medicamentoDTO2 = new MedicamentoDTO();
        assertThat(medicamentoDTO1).isNotEqualTo(medicamentoDTO2);
        medicamentoDTO2.setId(medicamentoDTO1.getId());
        assertThat(medicamentoDTO1).isEqualTo(medicamentoDTO2);
        medicamentoDTO2.setId(2L);
        assertThat(medicamentoDTO1).isNotEqualTo(medicamentoDTO2);
        medicamentoDTO1.setId(null);
        assertThat(medicamentoDTO1).isNotEqualTo(medicamentoDTO2);
    }
}
