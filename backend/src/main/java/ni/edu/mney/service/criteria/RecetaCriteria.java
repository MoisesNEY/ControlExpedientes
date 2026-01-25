package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.Receta} entity. This class is used
 * in {@link ni.edu.mney.web.rest.RecetaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /recetas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RecetaCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter dosis;

    private StringFilter frecuencia;

    private StringFilter duracion;

    private LongFilter medicamentoId;

    private LongFilter consultaId;

    private Boolean distinct;

    public RecetaCriteria() {}

    public RecetaCriteria(RecetaCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.dosis = other.optionalDosis().map(StringFilter::copy).orElse(null);
        this.frecuencia = other.optionalFrecuencia().map(StringFilter::copy).orElse(null);
        this.duracion = other.optionalDuracion().map(StringFilter::copy).orElse(null);
        this.medicamentoId = other.optionalMedicamentoId().map(LongFilter::copy).orElse(null);
        this.consultaId = other.optionalConsultaId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public RecetaCriteria copy() {
        return new RecetaCriteria(this);
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

    public StringFilter getDosis() {
        return dosis;
    }

    public Optional<StringFilter> optionalDosis() {
        return Optional.ofNullable(dosis);
    }

    public StringFilter dosis() {
        if (dosis == null) {
            setDosis(new StringFilter());
        }
        return dosis;
    }

    public void setDosis(StringFilter dosis) {
        this.dosis = dosis;
    }

    public StringFilter getFrecuencia() {
        return frecuencia;
    }

    public Optional<StringFilter> optionalFrecuencia() {
        return Optional.ofNullable(frecuencia);
    }

    public StringFilter frecuencia() {
        if (frecuencia == null) {
            setFrecuencia(new StringFilter());
        }
        return frecuencia;
    }

    public void setFrecuencia(StringFilter frecuencia) {
        this.frecuencia = frecuencia;
    }

    public StringFilter getDuracion() {
        return duracion;
    }

    public Optional<StringFilter> optionalDuracion() {
        return Optional.ofNullable(duracion);
    }

    public StringFilter duracion() {
        if (duracion == null) {
            setDuracion(new StringFilter());
        }
        return duracion;
    }

    public void setDuracion(StringFilter duracion) {
        this.duracion = duracion;
    }

    public LongFilter getMedicamentoId() {
        return medicamentoId;
    }

    public Optional<LongFilter> optionalMedicamentoId() {
        return Optional.ofNullable(medicamentoId);
    }

    public LongFilter medicamentoId() {
        if (medicamentoId == null) {
            setMedicamentoId(new LongFilter());
        }
        return medicamentoId;
    }

    public void setMedicamentoId(LongFilter medicamentoId) {
        this.medicamentoId = medicamentoId;
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
        final RecetaCriteria that = (RecetaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(dosis, that.dosis) &&
            Objects.equals(frecuencia, that.frecuencia) &&
            Objects.equals(duracion, that.duracion) &&
            Objects.equals(medicamentoId, that.medicamentoId) &&
            Objects.equals(consultaId, that.consultaId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dosis, frecuencia, duracion, medicamentoId, consultaId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecetaCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDosis().map(f -> "dosis=" + f + ", ").orElse("") +
            optionalFrecuencia().map(f -> "frecuencia=" + f + ", ").orElse("") +
            optionalDuracion().map(f -> "duracion=" + f + ", ").orElse("") +
            optionalMedicamentoId().map(f -> "medicamentoId=" + f + ", ").orElse("") +
            optionalConsultaId().map(f -> "consultaId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
