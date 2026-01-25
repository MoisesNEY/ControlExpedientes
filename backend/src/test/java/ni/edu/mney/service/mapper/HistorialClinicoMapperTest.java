package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.HistorialClinicoAsserts.*;
import static ni.edu.mney.domain.HistorialClinicoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistorialClinicoMapperTest {

    private HistorialClinicoMapper historialClinicoMapper;

    @BeforeEach
    void setUp() {
        historialClinicoMapper = new HistorialClinicoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getHistorialClinicoSample1();
        var actual = historialClinicoMapper.toEntity(historialClinicoMapper.toDto(expected));
        assertHistorialClinicoAllPropertiesEquals(expected, actual);
    }
}
