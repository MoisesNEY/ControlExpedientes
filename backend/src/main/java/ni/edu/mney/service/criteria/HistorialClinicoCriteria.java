package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.HistorialClinico} entity. This class is used
 * in {@link ni.edu.mney.web.rest.HistorialClinicoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /historial-clinicos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HistorialClinicoCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private ZonedDateTimeFilter fechaRegistro;

    private StringFilter descripcion;

    private LongFilter expedienteId;

    private Boolean distinct;

    public HistorialClinicoCriteria() {}

    public HistorialClinicoCriteria(HistorialClinicoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.fechaRegistro = other.optionalFechaRegistro().map(ZonedDateTimeFilter::copy).orElse(null);
        this.descripcion = other.optionalDescripcion().map(StringFilter::copy).orElse(null);
        this.expedienteId = other.optionalExpedienteId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public HistorialClinicoCriteria copy() {
        return new HistorialClinicoCriteria(this);
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

    public ZonedDateTimeFilter getFechaRegistro() {
        return fechaRegistro;
    }

    public Optional<ZonedDateTimeFilter> optionalFechaRegistro() {
        return Optional.ofNullable(fechaRegistro);
    }

    public ZonedDateTimeFilter fechaRegistro() {
        if (fechaRegistro == null) {
            setFechaRegistro(new ZonedDateTimeFilter());
        }
        return fechaRegistro;
    }

    public void setFechaRegistro(ZonedDateTimeFilter fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public StringFilter getDescripcion() {
        return descripcion;
    }

    public Optional<StringFilter> optionalDescripcion() {
        return Optional.ofNullable(descripcion);
    }

    public StringFilter descripcion() {
        if (descripcion == null) {
            setDescripcion(new StringFilter());
        }
        return descripcion;
    }

    public void setDescripcion(StringFilter descripcion) {
        this.descripcion = descripcion;
    }

    public LongFilter getExpedienteId() {
        return expedienteId;
    }

    public Optional<LongFilter> optionalExpedienteId() {
        return Optional.ofNullable(expedienteId);
    }

    public LongFilter expedienteId() {
        if (expedienteId == null) {
            setExpedienteId(new LongFilter());
        }
        return expedienteId;
    }

    public void setExpedienteId(LongFilter expedienteId) {
        this.expedienteId = expedienteId;
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
        final HistorialClinicoCriteria that = (HistorialClinicoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(fechaRegistro, that.fechaRegistro) &&
            Objects.equals(descripcion, that.descripcion) &&
            Objects.equals(expedienteId, that.expedienteId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fechaRegistro, descripcion, expedienteId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HistorialClinicoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalFechaRegistro().map(f -> "fechaRegistro=" + f + ", ").orElse("") +
            optionalDescripcion().map(f -> "descripcion=" + f + ", ").orElse("") +
            optionalExpedienteId().map(f -> "expedienteId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
