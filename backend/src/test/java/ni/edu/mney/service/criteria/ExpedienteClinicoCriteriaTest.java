package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ExpedienteClinicoCriteriaTest {

    @Test
    void newExpedienteClinicoCriteriaHasAllFiltersNullTest() {
        var expedienteClinicoCriteria = new ExpedienteClinicoCriteria();
        assertThat(expedienteClinicoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void expedienteClinicoCriteriaFluentMethodsCreatesFiltersTest() {
        var expedienteClinicoCriteria = new ExpedienteClinicoCriteria();

        setAllFilters(expedienteClinicoCriteria);

        assertThat(expedienteClinicoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void expedienteClinicoCriteriaCopyCreatesNullFilterTest() {
        var expedienteClinicoCriteria = new ExpedienteClinicoCriteria();
        var copy = expedienteClinicoCriteria.copy();

        assertThat(expedienteClinicoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(expedienteClinicoCriteria)
        );
    }

    @Test
    void expedienteClinicoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var expedienteClinicoCriteria = new ExpedienteClinicoCriteria();
        setAllFilters(expedienteClinicoCriteria);

        var copy = expedienteClinicoCriteria.copy();

        assertThat(expedienteClinicoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(expedienteClinicoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var expedienteClinicoCriteria = new ExpedienteClinicoCriteria();

        assertThat(expedienteClinicoCriteria).hasToString("ExpedienteClinicoCriteria{}");
    }

    private static void setAllFilters(ExpedienteClinicoCriteria expedienteClinicoCriteria) {
        expedienteClinicoCriteria.id();
        expedienteClinicoCriteria.numeroExpediente();
        expedienteClinicoCriteria.fechaApertura();
        expedienteClinicoCriteria.observaciones();
        expedienteClinicoCriteria.consultaId();
        expedienteClinicoCriteria.historialId();
        expedienteClinicoCriteria.pacienteId();
        expedienteClinicoCriteria.distinct();
    }

    private static Condition<ExpedienteClinicoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNumeroExpediente()) &&
                condition.apply(criteria.getFechaApertura()) &&
                condition.apply(criteria.getObservaciones()) &&
                condition.apply(criteria.getConsultaId()) &&
                condition.apply(criteria.getHistorialId()) &&
                condition.apply(criteria.getPacienteId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ExpedienteClinicoCriteria> copyFiltersAre(
        ExpedienteClinicoCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNumeroExpediente(), copy.getNumeroExpediente()) &&
                condition.apply(criteria.getFechaApertura(), copy.getFechaApertura()) &&
                condition.apply(criteria.getObservaciones(), copy.getObservaciones()) &&
                condition.apply(criteria.getConsultaId(), copy.getConsultaId()) &&
                condition.apply(criteria.getHistorialId(), copy.getHistorialId()) &&
                condition.apply(criteria.getPacienteId(), copy.getPacienteId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
