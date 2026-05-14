package ni.edu.mney.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

class ExceptionTranslatorUnitTest {

    @Test
    void wrapAndCustomizeProblemMapsIllegalArgumentExceptionToBadRequest() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);

        ExceptionTranslator exceptionTranslator = new ExceptionTranslator(environment);

        var problem = exceptionTranslator.wrapAndCustomizeProblem(new IllegalArgumentException("validation failed"), null);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("validation failed");
        assertThat(problem.getProperties()).containsEntry("message", "error.http.400");
    }
}