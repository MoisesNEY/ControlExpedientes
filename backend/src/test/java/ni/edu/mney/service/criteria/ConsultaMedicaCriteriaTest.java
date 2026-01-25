package ni.edu.mney.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ConsultaMedicaCriteriaTest {

    @Test
    void newConsultaMedicaCriteriaHasAllFiltersNullTest() {
        var consultaMedicaCriteria = new ConsultaMedicaCriteria();
        assertThat(consultaMedicaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void consultaMedicaCriteriaFluentMethodsCreatesFiltersTest() {
        var consultaMedicaCriteria = new ConsultaMedicaCriteria();

        setAllFilters(consultaMedicaCriteria);

        assertThat(consultaMedicaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void consultaMedicaCriteriaCopyCreatesNullFilterTest() {
        var consultaMedicaCriteria = new ConsultaMedicaCriteria();
        var copy = consultaMedicaCriteria.copy();

        assertThat(consultaMedicaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(consultaMedicaCriteria)
        );
    }

    @Test
    void consultaMedicaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var consultaMedicaCriteria = new ConsultaMedicaCriteria();
        setAllFilters(consultaMedicaCriteria);

        var copy = consultaMedicaCriteria.copy();

        assertThat(consultaMedicaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(consultaMedicaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var consultaMedicaCriteria = new ConsultaMedicaCriteria();

        assertThat(consultaMedicaCriteria).hasToString("ConsultaMedicaCriteria{}");
    }

    private static void setAllFilters(ConsultaMedicaCriteria consultaMedicaCriteria) {
        consultaMedicaCriteria.id();
        consultaMedicaCriteria.fechaConsulta();
        consultaMedicaCriteria.motivoConsulta();
        consultaMedicaCriteria.notasMedicas();
        consultaMedicaCriteria.diagnosticoId();
        consultaMedicaCriteria.tratamientoId();
        consultaMedicaCriteria.signosVitalesId();
        consultaMedicaCriteria.recetaId();
        consultaMedicaCriteria.userId();
        consultaMedicaCriteria.expedienteId();
        consultaMedicaCriteria.distinct();
    }

    private static Condition<ConsultaMedicaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getFechaConsulta()) &&
                condition.apply(criteria.getMotivoConsulta()) &&
                condition.apply(criteria.getNotasMedicas()) &&
                condition.apply(criteria.getDiagnosticoId()) &&
                condition.apply(criteria.getTratamientoId()) &&
                condition.apply(criteria.getSignosVitalesId()) &&
                condition.apply(criteria.getRecetaId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getExpedienteId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ConsultaMedicaCriteria> copyFiltersAre(
        ConsultaMedicaCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getFechaConsulta(), copy.getFechaConsulta()) &&
                condition.apply(criteria.getMotivoConsulta(), copy.getMotivoConsulta()) &&
                condition.apply(criteria.getNotasMedicas(), copy.getNotasMedicas()) &&
                condition.apply(criteria.getDiagnosticoId(), copy.getDiagnosticoId()) &&
                condition.apply(criteria.getTratamientoId(), copy.getTratamientoId()) &&
                condition.apply(criteria.getSignosVitalesId(), copy.getSignosVitalesId()) &&
                condition.apply(criteria.getRecetaId(), copy.getRecetaId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getExpedienteId(), copy.getExpedienteId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
