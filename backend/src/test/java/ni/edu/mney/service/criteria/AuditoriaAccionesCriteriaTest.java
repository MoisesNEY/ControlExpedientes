package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AuditoriaAccionesCriteriaTest {

    @Test
    void newAuditoriaAccionesCriteriaHasAllFiltersNullTest() {
        var auditoriaAccionesCriteria = new AuditoriaAccionesCriteria();
        assertThat(auditoriaAccionesCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void auditoriaAccionesCriteriaFluentMethodsCreatesFiltersTest() {
        var auditoriaAccionesCriteria = new AuditoriaAccionesCriteria();

        setAllFilters(auditoriaAccionesCriteria);

        assertThat(auditoriaAccionesCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void auditoriaAccionesCriteriaCopyCreatesNullFilterTest() {
        var auditoriaAccionesCriteria = new AuditoriaAccionesCriteria();
        var copy = auditoriaAccionesCriteria.copy();

        assertThat(auditoriaAccionesCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(auditoriaAccionesCriteria)
        );
    }

    @Test
    void auditoriaAccionesCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var auditoriaAccionesCriteria = new AuditoriaAccionesCriteria();
        setAllFilters(auditoriaAccionesCriteria);

        var copy = auditoriaAccionesCriteria.copy();

        assertThat(auditoriaAccionesCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(auditoriaAccionesCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var auditoriaAccionesCriteria = new AuditoriaAccionesCriteria();

        assertThat(auditoriaAccionesCriteria).hasToString("AuditoriaAccionesCriteria{}");
    }

    private static void setAllFilters(AuditoriaAccionesCriteria auditoriaAccionesCriteria) {
        auditoriaAccionesCriteria.id();
        auditoriaAccionesCriteria.entidad();
        auditoriaAccionesCriteria.accion();
        auditoriaAccionesCriteria.fecha();
        auditoriaAccionesCriteria.descripcion();
        auditoriaAccionesCriteria.userId();
        auditoriaAccionesCriteria.distinct();
    }

    private static Condition<AuditoriaAccionesCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getEntidad()) &&
                condition.apply(criteria.getAccion()) &&
                condition.apply(criteria.getFecha()) &&
                condition.apply(criteria.getDescripcion()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AuditoriaAccionesCriteria> copyFiltersAre(
        AuditoriaAccionesCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getEntidad(), copy.getEntidad()) &&
                condition.apply(criteria.getAccion(), copy.getAccion()) &&
                condition.apply(criteria.getFecha(), copy.getFecha()) &&
                condition.apply(criteria.getDescripcion(), copy.getDescripcion()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
