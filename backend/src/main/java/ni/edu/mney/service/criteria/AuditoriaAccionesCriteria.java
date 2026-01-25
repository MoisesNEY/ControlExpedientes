package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.AuditoriaAcciones} entity. This class is used
 * in {@link ni.edu.mney.web.rest.AuditoriaAccionesResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /auditoria-acciones?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditoriaAccionesCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter entidad;

    private StringFilter accion;

    private ZonedDateTimeFilter fecha;

    private StringFilter descripcion;

    private StringFilter userId;

    private Boolean distinct;

    public AuditoriaAccionesCriteria() {}

    public AuditoriaAccionesCriteria(AuditoriaAccionesCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.entidad = other.optionalEntidad().map(StringFilter::copy).orElse(null);
        this.accion = other.optionalAccion().map(StringFilter::copy).orElse(null);
        this.fecha = other.optionalFecha().map(ZonedDateTimeFilter::copy).orElse(null);
        this.descripcion = other.optionalDescripcion().map(StringFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(StringFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AuditoriaAccionesCriteria copy() {
        return new AuditoriaAccionesCriteria(this);
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

    public StringFilter getEntidad() {
        return entidad;
    }

    public Optional<StringFilter> optionalEntidad() {
        return Optional.ofNullable(entidad);
    }

    public StringFilter entidad() {
        if (entidad == null) {
            setEntidad(new StringFilter());
        }
        return entidad;
    }

    public void setEntidad(StringFilter entidad) {
        this.entidad = entidad;
    }

    public StringFilter getAccion() {
        return accion;
    }

    public Optional<StringFilter> optionalAccion() {
        return Optional.ofNullable(accion);
    }

    public StringFilter accion() {
        if (accion == null) {
            setAccion(new StringFilter());
        }
        return accion;
    }

    public void setAccion(StringFilter accion) {
        this.accion = accion;
    }

    public ZonedDateTimeFilter getFecha() {
        return fecha;
    }

    public Optional<ZonedDateTimeFilter> optionalFecha() {
        return Optional.ofNullable(fecha);
    }

    public ZonedDateTimeFilter fecha() {
        if (fecha == null) {
            setFecha(new ZonedDateTimeFilter());
        }
        return fecha;
    }

    public void setFecha(ZonedDateTimeFilter fecha) {
        this.fecha = fecha;
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

    public StringFilter getUserId() {
        return userId;
    }

    public Optional<StringFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public StringFilter userId() {
        if (userId == null) {
            setUserId(new StringFilter());
        }
        return userId;
    }

    public void setUserId(StringFilter userId) {
        this.userId = userId;
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
        final AuditoriaAccionesCriteria that = (AuditoriaAccionesCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(entidad, that.entidad) &&
            Objects.equals(accion, that.accion) &&
            Objects.equals(fecha, that.fecha) &&
            Objects.equals(descripcion, that.descripcion) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entidad, accion, fecha, descripcion, userId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditoriaAccionesCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalEntidad().map(f -> "entidad=" + f + ", ").orElse("") +
            optionalAccion().map(f -> "accion=" + f + ", ").orElse("") +
            optionalFecha().map(f -> "fecha=" + f + ", ").orElse("") +
            optionalDescripcion().map(f -> "descripcion=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
