package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.SignosVitales} entity. This class is used
 * in {@link ni.edu.mney.web.rest.SignosVitalesResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /signos-vitales?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignosVitalesCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private DoubleFilter peso;

    private DoubleFilter altura;

    private StringFilter presionArterial;

    private DoubleFilter temperatura;

    private IntegerFilter frecuenciaCardiaca;

    private LongFilter consultaId;

    private Boolean distinct;

    public SignosVitalesCriteria() {}

    public SignosVitalesCriteria(SignosVitalesCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.peso = other.optionalPeso().map(DoubleFilter::copy).orElse(null);
        this.altura = other.optionalAltura().map(DoubleFilter::copy).orElse(null);
        this.presionArterial = other.optionalPresionArterial().map(StringFilter::copy).orElse(null);
        this.temperatura = other.optionalTemperatura().map(DoubleFilter::copy).orElse(null);
        this.frecuenciaCardiaca = other.optionalFrecuenciaCardiaca().map(IntegerFilter::copy).orElse(null);
        this.consultaId = other.optionalConsultaId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SignosVitalesCriteria copy() {
        return new SignosVitalesCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public DoubleFilter getPeso() {
        return peso;
    }

    public Optional<DoubleFilter> optionalPeso() {
        return Optional.ofNullable(peso);
    }

    public DoubleFilter peso() {
        if (peso == null) {
            setPeso(new DoubleFilter());
        }
        return peso;
    }

    public void setPeso(DoubleFilter peso) {
        this.peso = peso;
    }

    public DoubleFilter getAltura() {
        return altura;
    }

    public Optional<DoubleFilter> optionalAltura() {
        return Optional.ofNullable(altura);
    }

    public DoubleFilter altura() {
        if (altura == null) {
            setAltura(new DoubleFilter());
        }
        return altura;
    }

    public void setAltura(DoubleFilter altura) {
        this.altura = altura;
    }

    public StringFilter getPresionArterial() {
        return presionArterial;
    }

    public Optional<StringFilter> optionalPresionArterial() {
        return Optional.ofNullable(presionArterial);
    }

    public StringFilter presionArterial() {
        if (presionArterial == null) {
            setPresionArterial(new StringFilter());
        }
        return presionArterial;
    }

    public void setPresionArterial(StringFilter presionArterial) {
        this.presionArterial = presionArterial;
    }

    public DoubleFilter getTemperatura() {
        return temperatura;
    }

    public Optional<DoubleFilter> optionalTemperatura() {
        return Optional.ofNullable(temperatura);
    }

    public DoubleFilter temperatura() {
        if (temperatura == null) {
            setTemperatura(new DoubleFilter());
        }
        return temperatura;
    }

    public void setTemperatura(DoubleFilter temperatura) {
        this.temperatura = temperatura;
    }

    public IntegerFilter getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }

    public Optional<IntegerFilter> optionalFrecuenciaCardiaca() {
        return Optional.ofNullable(frecuenciaCardiaca);
    }

    public IntegerFilter frecuenciaCardiaca() {
        if (frecuenciaCardiaca == null) {
            setFrecuenciaCardiaca(new IntegerFilter());
        }
        return frecuenciaCardiaca;
    }

    public void setFrecuenciaCardiaca(IntegerFilter frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }

    public LongFilter getConsultaId() {
        return consultaId;
    }

    public Optional<LongFilter> optionalConsultaId() {
        return Optional.ofNullable(consultaId);
    }

    public LongFilter consultaId() {
        if (consultaId == null) {
            setConsultaId(new LongFilter());
        }
        return consultaId;
    }

    public void setConsultaId(LongFilter consultaId) {
        this.consultaId = consultaId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SignosVitalesCriteria that = (SignosVitalesCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(peso, that.peso) &&
            Objects.equals(altura, that.altura) &&
            Objects.equals(presionArterial, that.presionArterial) &&
            Objects.equals(temperatura, that.temperatura) &&
            Objects.equals(frecuenciaCardiaca, that.frecuenciaCardiaca) &&
            Objects.equals(consultaId, that.consultaId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, peso, altura, presionArterial, temperatura, frecuenciaCardiaca, consultaId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignosVitalesCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalPeso().map(f -> "peso=" + f + ", ").orElse("") +
            optionalAltura().map(f -> "altura=" + f + ", ").orElse("") +
            optionalPresionArterial().map(f -> "presionArterial=" + f + ", ").orElse("") +
            optionalTemperatura().map(f -> "temperatura=" + f + ", ").orElse("") +
            optionalFrecuenciaCardiaca().map(f -> "frecuenciaCardiaca=" + f + ", ").orElse("") +
            optionalConsultaId().map(f -> "consultaId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
