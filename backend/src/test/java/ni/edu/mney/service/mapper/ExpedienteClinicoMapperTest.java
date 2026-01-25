package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.ExpedienteClinicoAsserts.*;
import static ni.edu.mney.domain.ExpedienteClinicoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpedienteClinicoMapperTest {

    private ExpedienteClinicoMapper expedienteClinicoMapper;

    @BeforeEach
    void setUp() {
        expedienteClinicoMapper = new ExpedienteClinicoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getExpedienteClinicoSample1();
        var actual = expedienteClinicoMapper.toEntity(expedienteClinicoMapper.toDto(expected));
        assertExpedienteClinicoAllPropertiesEquals(expected, actual);
    }
}
