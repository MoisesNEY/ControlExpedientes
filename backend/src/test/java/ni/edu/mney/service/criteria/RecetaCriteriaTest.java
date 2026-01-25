package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class RecetaCriteriaTest {

    @Test
    void newRecetaCriteriaHasAllFiltersNullTest() {
        var recetaCriteria = new RecetaCriteria();
        assertThat(recetaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void recetaCriteriaFluentMethodsCreatesFiltersTest() {
        var recetaCriteria = new RecetaCriteria();

        setAllFilters(recetaCriteria);

        assertThat(recetaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void recetaCriteriaCopyCreatesNullFilterTest() {
        var recetaCriteria = new RecetaCriteria();
        var copy = recetaCriteria.copy();

        assertThat(recetaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(recetaCriteria)
        );
    }

    @Test
    void recetaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var recetaCriteria = new RecetaCriteria();
        setAllFilters(recetaCriteria);

        var copy = recetaCriteria.copy();

        assertThat(recetaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(recetaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var recetaCriteria = new RecetaCriteria();

        assertThat(recetaCriteria).hasToString("RecetaCriteria{}");
    }

    private static void setAllFilters(RecetaCriteria recetaCriteria) {
        recetaCriteria.id();
        recetaCriteria.dosis();
        recetaCriteria.frecuencia();
        recetaCriteria.duracion();
        recetaCriteria.medicamentoId();
        recetaCriteria.consultaId();
        recetaCriteria.distinct();
    }

    private static Condition<RecetaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDosis()) &&
                condition.apply(criteria.getFrecuencia()) &&
                condition.apply(criteria.getDuracion()) &&
                condition.apply(criteria.getMedicamentoId()) &&
                condition.apply(criteria.getConsultaId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<RecetaCriteria> copyFiltersAre(RecetaCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDosis(), copy.getDosis()) &&
                condition.apply(criteria.getFrecuencia(), copy.getFrecuencia()) &&
                condition.apply(criteria.getDuracion(), copy.getDuracion()) &&
                condition.apply(criteria.getMedicamentoId(), copy.getMedicamentoId()) &&
                condition.apply(criteria.getConsultaId(), copy.getConsultaId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
