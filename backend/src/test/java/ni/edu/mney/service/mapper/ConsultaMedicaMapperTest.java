package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.ConsultaMedicaAsserts.*;
import static ni.edu.mney.domain.ConsultaMedicaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultaMedicaMapperTest {

    private ConsultaMedicaMapper consultaMedicaMapper;

    @BeforeEach
    void setUp() {
        consultaMedicaMapper = new ConsultaMedicaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getConsultaMedicaSample1();
        var actual = consultaMedicaMapper.toEntity(consultaMedicaMapper.toDto(expected));
        assertConsultaMedicaAllPropertiesEquals(expected, actual);
    }
}
