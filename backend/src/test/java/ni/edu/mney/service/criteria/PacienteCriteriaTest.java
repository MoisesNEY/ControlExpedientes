package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class PacienteCriteriaTest {

    @Test
    void newPacienteCriteriaHasAllFiltersNullTest() {
        var pacienteCriteria = new PacienteCriteria();
        assertThat(pacienteCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void pacienteCriteriaFluentMethodsCreatesFiltersTest() {
        var pacienteCriteria = new PacienteCriteria();

        setAllFilters(pacienteCriteria);

        assertThat(pacienteCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void pacienteCriteriaCopyCreatesNullFilterTest() {
        var pacienteCriteria = new PacienteCriteria();
        var copy = pacienteCriteria.copy();

        assertThat(pacienteCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(pacienteCriteria)
        );
    }

    @Test
    void pacienteCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var pacienteCriteria = new PacienteCriteria();
        setAllFilters(pacienteCriteria);

        var copy = pacienteCriteria.copy();

        assertThat(pacienteCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(pacienteCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var pacienteCriteria = new PacienteCriteria();

        assertThat(pacienteCriteria).hasToString("PacienteCriteria{}");
    }

    private static void setAllFilters(PacienteCriteria pacienteCriteria) {
        pacienteCriteria.id();
        pacienteCriteria.codigo();
        pacienteCriteria.nombres();
        pacienteCriteria.apellidos();
        pacienteCriteria.sexo();
        pacienteCriteria.fechaNacimiento();
        pacienteCriteria.cedula();
        pacienteCriteria.telefono();
        pacienteCriteria.direccion();
        pacienteCriteria.estadoCivil();
        pacienteCriteria.email();
        pacienteCriteria.activo();
        pacienteCriteria.expedienteId();
        pacienteCriteria.citaId();
        pacienteCriteria.distinct();
    }

    private static Condition<PacienteCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getCodigo()) &&
                condition.apply(criteria.getNombres()) &&
                condition.apply(criteria.getApellidos()) &&
                condition.apply(criteria.getSexo()) &&
                condition.apply(criteria.getFechaNacimiento()) &&
                condition.apply(criteria.getCedula()) &&
                condition.apply(criteria.getTelefono()) &&
                condition.apply(criteria.getDireccion()) &&
                condition.apply(criteria.getEstadoCivil()) &&
                condition.apply(criteria.getEmail()) &&
                condition.apply(criteria.getActivo()) &&
                condition.apply(criteria.getExpedienteId()) &&
                condition.apply(criteria.getCitaId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<PacienteCriteria> copyFiltersAre(PacienteCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getCodigo(), copy.getCodigo()) &&
                condition.apply(criteria.getNombres(), copy.getNombres()) &&
                condition.apply(criteria.getApellidos(), copy.getApellidos()) &&
                condition.apply(criteria.getSexo(), copy.getSexo()) &&
                condition.apply(criteria.getFechaNacimiento(), copy.getFechaNacimiento()) &&
                condition.apply(criteria.getCedula(), copy.getCedula()) &&
                condition.apply(criteria.getTelefono(), copy.getTelefono()) &&
                condition.apply(criteria.getDireccion(), copy.getDireccion()) &&
                condition.apply(criteria.getEstadoCivil(), copy.getEstadoCivil()) &&
                condition.apply(criteria.getEmail(), copy.getEmail()) &&
                condition.apply(criteria.getActivo(), copy.getActivo()) &&
                condition.apply(criteria.getExpedienteId(), copy.getExpedienteId()) &&
                condition.apply(criteria.getCitaId(), copy.getCitaId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
