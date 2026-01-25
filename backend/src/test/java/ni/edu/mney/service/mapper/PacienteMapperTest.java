package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.PacienteAsserts.*;
import static ni.edu.mney.domain.PacienteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PacienteMapperTest {

    private PacienteMapper pacienteMapper;

    @BeforeEach
    void setUp() {
        pacienteMapper = new PacienteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPacienteSample1();
        var actual = pacienteMapper.toEntity(pacienteMapper.toDto(expected));
        assertPacienteAllPropertiesEquals(expected, actual);
    }
}
