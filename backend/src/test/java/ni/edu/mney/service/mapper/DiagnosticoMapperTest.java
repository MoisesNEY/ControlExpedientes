package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.DiagnosticoAsserts.*;
import static ni.edu.mney.domain.DiagnosticoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiagnosticoMapperTest {

    private DiagnosticoMapper diagnosticoMapper;

    @BeforeEach
    void setUp() {
        diagnosticoMapper = new DiagnosticoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDiagnosticoSample1();
        var actual = diagnosticoMapper.toEntity(diagnosticoMapper.toDto(expected));
        assertDiagnosticoAllPropertiesEquals(expected, actual);
    }
}
