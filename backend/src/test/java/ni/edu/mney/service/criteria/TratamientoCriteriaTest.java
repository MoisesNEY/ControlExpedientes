package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class TratamientoCriteriaTest {

    @Test
    void newTratamientoCriteriaHasAllFiltersNullTest() {
        var tratamientoCriteria = new TratamientoCriteria();
        assertThat(tratamientoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void tratamientoCriteriaFluentMethodsCreatesFiltersTest() {
        var tratamientoCriteria = new TratamientoCriteria();

        setAllFilters(tratamientoCriteria);

        assertThat(tratamientoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void tratamientoCriteriaCopyCreatesNullFilterTest() {
        var tratamientoCriteria = new TratamientoCriteria();
        var copy = tratamientoCriteria.copy();

        assertThat(tratamientoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(tratamientoCriteria)
        );
    }

    @Test
    void tratamientoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var tratamientoCriteria = new TratamientoCriteria();
        setAllFilters(tratamientoCriteria);

        var copy = tratamientoCriteria.copy();

        assertThat(tratamientoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(tratamientoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var tratamientoCriteria = new TratamientoCriteria();

        assertThat(tratamientoCriteria).hasToString("TratamientoCriteria{}");
    }

    private static void setAllFilters(TratamientoCriteria tratamientoCriteria) {
        tratamientoCriteria.id();
        tratamientoCriteria.indicaciones();
        tratamientoCriteria.duracionDias();
        tratamientoCriteria.consultaId();
        tratamientoCriteria.distinct();
    }

    private static Condition<TratamientoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getIndicaciones()) &&
                condition.apply(criteria.getDuracionDias()) &&
                condition.apply(criteria.getConsultaId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<TratamientoCriteria> copyFiltersAre(TratamientoCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getIndicaciones(), copy.getIndicaciones()) &&
                condition.apply(criteria.getDuracionDias(), copy.getDuracionDias()) &&
                condition.apply(criteria.getConsultaId(), copy.getConsultaId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
