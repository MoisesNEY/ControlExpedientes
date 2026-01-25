package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class HistorialClinicoCriteriaTest {

    @Test
    void newHistorialClinicoCriteriaHasAllFiltersNullTest() {
        var historialClinicoCriteria = new HistorialClinicoCriteria();
        assertThat(historialClinicoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void historialClinicoCriteriaFluentMethodsCreatesFiltersTest() {
        var historialClinicoCriteria = new HistorialClinicoCriteria();

        setAllFilters(historialClinicoCriteria);

        assertThat(historialClinicoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void historialClinicoCriteriaCopyCreatesNullFilterTest() {
        var historialClinicoCriteria = new HistorialClinicoCriteria();
        var copy = historialClinicoCriteria.copy();

        assertThat(historialClinicoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(historialClinicoCriteria)
        );
    }

    @Test
    void historialClinicoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var historialClinicoCriteria = new HistorialClinicoCriteria();
        setAllFilters(historialClinicoCriteria);

        var copy = historialClinicoCriteria.copy();

        assertThat(historialClinicoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(historialClinicoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var historialClinicoCriteria = new HistorialClinicoCriteria();

        assertThat(historialClinicoCriteria).hasToString("HistorialClinicoCriteria{}");
    }

    private static void setAllFilters(HistorialClinicoCriteria historialClinicoCriteria) {
        historialClinicoCriteria.id();
        historialClinicoCriteria.fechaRegistro();
        historialClinicoCriteria.descripcion();
        historialClinicoCriteria.expedienteId();
        historialClinicoCriteria.distinct();
    }

    private static Condition<HistorialClinicoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getFechaRegistro()) &&
                condition.apply(criteria.getDescripcion()) &&
                condition.apply(criteria.getExpedienteId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<HistorialClinicoCriteria> copyFiltersAre(
        HistorialClinicoCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getFechaRegistro(), copy.getFechaRegistro()) &&
                condition.apply(criteria.getDescripcion(), copy.getDescripcion()) &&
                condition.apply(criteria.getExpedienteId(), copy.getExpedienteId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
