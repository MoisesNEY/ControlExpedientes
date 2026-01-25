package ni.edu.mney.service.mapper;

import static ni.edu.mney.domain.RecetaAsserts.*;
import static ni.edu.mney.domain.RecetaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecetaMapperTest {

    private RecetaMapper recetaMapper;

    @BeforeEach
    void setUp() {
        recetaMapper = new RecetaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRecetaSample1();
        var actual = recetaMapper.toEntity(recetaMapper.toDto(expected));
        assertRecetaAllPropertiesEquals(expected, actual);
    }
}
