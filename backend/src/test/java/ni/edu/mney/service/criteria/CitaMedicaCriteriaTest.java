package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CitaMedicaCriteriaTest {

    @Test
    void newCitaMedicaCriteriaHasAllFiltersNullTest() {
        var citaMedicaCriteria = new CitaMedicaCriteria();
        assertThat(citaMedicaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void citaMedicaCriteriaFluentMethodsCreatesFiltersTest() {
        var citaMedicaCriteria = new CitaMedicaCriteria();

        setAllFilters(citaMedicaCriteria);

        assertThat(citaMedicaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void citaMedicaCriteriaCopyCreatesNullFilterTest() {
        var citaMedicaCriteria = new CitaMedicaCriteria();
        var copy = citaMedicaCriteria.copy();

        assertThat(citaMedicaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(citaMedicaCriteria)
        );
    }

    @Test
    void citaMedicaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var citaMedicaCriteria = new CitaMedicaCriteria();
        setAllFilters(citaMedicaCriteria);

        var copy = citaMedicaCriteria.copy();

        assertThat(citaMedicaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(citaMedicaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var citaMedicaCriteria = new CitaMedicaCriteria();

        assertThat(citaMedicaCriteria).hasToString("CitaMedicaCriteria{}");
    }

    private static void setAllFilters(CitaMedicaCriteria citaMedicaCriteria) {
        citaMedicaCriteria.id();
        citaMedicaCriteria.fechaHora();
        citaMedicaCriteria.estado();
        citaMedicaCriteria.observaciones();
        citaMedicaCriteria.userId();
        citaMedicaCriteria.pacienteId();
        citaMedicaCriteria.distinct();
    }

    private static Condition<CitaMedicaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getFechaHora()) &&
                condition.apply(criteria.getEstado()) &&
                condition.apply(criteria.getObservaciones()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getPacienteId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CitaMedicaCriteria> copyFiltersAre(CitaMedicaCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getFechaHora(), copy.getFechaHora()) &&
                condition.apply(criteria.getEstado(), copy.getEstado()) &&
                condition.apply(criteria.getObservaciones(), copy.getObservaciones()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getPacienteId(), copy.getPacienteId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
