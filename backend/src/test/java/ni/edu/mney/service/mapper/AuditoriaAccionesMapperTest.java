package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.AuditoriaAccionesAsserts.*;
import static ni.edu.mney.domain.AuditoriaAccionesTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditoriaAccionesMapperTest {

    private AuditoriaAccionesMapper auditoriaAccionesMapper;

    @BeforeEach
    void setUp() {
        auditoriaAccionesMapper = new AuditoriaAccionesMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAuditoriaAccionesSample1();
        var actual = auditoriaAccionesMapper.toEntity(auditoriaAccionesMapper.toDto(expected));
        assertAuditoriaAccionesAllPropertiesEquals(expected, actual);
    }
}
