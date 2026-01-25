package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MedicamentoCriteriaTest {

    @Test
    void newMedicamentoCriteriaHasAllFiltersNullTest() {
        var medicamentoCriteria = new MedicamentoCriteria();
        assertThat(medicamentoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void medicamentoCriteriaFluentMethodsCreatesFiltersTest() {
        var medicamentoCriteria = new MedicamentoCriteria();

        setAllFilters(medicamentoCriteria);

        assertThat(medicamentoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void medicamentoCriteriaCopyCreatesNullFilterTest() {
        var medicamentoCriteria = new MedicamentoCriteria();
        var copy = medicamentoCriteria.copy();

        assertThat(medicamentoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(medicamentoCriteria)
        );
    }

    @Test
    void medicamentoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var medicamentoCriteria = new MedicamentoCriteria();
        setAllFilters(medicamentoCriteria);

        var copy = medicamentoCriteria.copy();

        assertThat(medicamentoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(medicamentoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var medicamentoCriteria = new MedicamentoCriteria();

        assertThat(medicamentoCriteria).hasToString("MedicamentoCriteria{}");
    }

    private static void setAllFilters(MedicamentoCriteria medicamentoCriteria) {
        medicamentoCriteria.id();
        medicamentoCriteria.nombre();
        medicamentoCriteria.descripcion();
        medicamentoCriteria.stock();
        medicamentoCriteria.distinct();
    }

    private static Condition<MedicamentoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getDescripcion()) &&
                condition.apply(criteria.getStock()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MedicamentoCriteria> copyFiltersAre(MedicamentoCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getDescripcion(), copy.getDescripcion()) &&
                condition.apply(criteria.getStock(), copy.getStock()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
