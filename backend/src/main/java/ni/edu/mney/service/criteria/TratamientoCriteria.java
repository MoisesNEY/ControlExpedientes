package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.Tratamiento} entity. This class is used
 * in {@link ni.edu.mney.web.rest.TratamientoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /tratamientos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TratamientoCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter indicaciones;

    private IntegerFilter duracionDias;

    private LongFilter consultaId;

    private Boolean distinct;

    public TratamientoCriteria() {}

    public TratamientoCriteria(TratamientoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.indicaciones = other.optionalIndicaciones().map(StringFilter::copy).orElse(null);
        this.duracionDias = other.optionalDuracionDias().map(IntegerFilter::copy).orElse(null);
        this.consultaId = other.optionalConsultaId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public TratamientoCriteria copy() {
        return new TratamientoCriteria(this);
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

    public StringFilter getIndicaciones() {
        return indicaciones;
    }

    public Optional<StringFilter> optionalIndicaciones() {
        return Optional.ofNullable(indicaciones);
    }

    public StringFilter indicaciones() {
        if (indicaciones == null) {
            setIndicaciones(new StringFilter());
        }
        return indicaciones;
    }

    public void setIndicaciones(StringFilter indicaciones) {
        this.indicaciones = indicaciones;
    }

    public IntegerFilter getDuracionDias() {
        return duracionDias;
    }

    public Optional<IntegerFilter> optionalDuracionDias() {
        return Optional.ofNullable(duracionDias);
    }

    public IntegerFilter duracionDias() {
        if (duracionDias == null) {
            setDuracionDias(new IntegerFilter());
        }
        return duracionDias;
    }

    public void setDuracionDias(IntegerFilter duracionDias) {
        this.duracionDias = duracionDias;
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
        final TratamientoCriteria that = (TratamientoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(indicaciones, that.indicaciones) &&
            Objects.equals(duracionDias, that.duracionDias) &&
            Objects.equals(consultaId, that.consultaId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, indicaciones, duracionDias, consultaId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TratamientoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalIndicaciones().map(f -> "indicaciones=" + f + ", ").orElse("") +
            optionalDuracionDias().map(f -> "duracionDias=" + f + ", ").orElse("") +
            optionalConsultaId().map(f -> "consultaId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
