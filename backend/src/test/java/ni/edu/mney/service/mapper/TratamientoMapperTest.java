package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.TratamientoAsserts.*;
import static ni.edu.mney.domain.TratamientoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TratamientoMapperTest {

    private TratamientoMapper tratamientoMapper;

    @BeforeEach
    void setUp() {
        tratamientoMapper = new TratamientoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTratamientoSample1();
        var actual = tratamientoMapper.toEntity(tratamientoMapper.toDto(expected));
        assertTratamientoAllPropertiesEquals(expected, actual);
    }
}
