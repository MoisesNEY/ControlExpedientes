package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.CitaMedicaAsserts.*;
import static ni.edu.mney.domain.CitaMedicaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CitaMedicaMapperTest {

    private CitaMedicaMapper citaMedicaMapper;

    @BeforeEach
    void setUp() {
        citaMedicaMapper = new CitaMedicaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCitaMedicaSample1();
        var actual = citaMedicaMapper.toEntity(citaMedicaMapper.toDto(expected));
        assertCitaMedicaAllPropertiesEquals(expected, actual);
    }
}
