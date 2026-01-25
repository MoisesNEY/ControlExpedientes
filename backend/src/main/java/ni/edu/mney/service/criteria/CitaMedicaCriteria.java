package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.domain.enumeration.EstadoCita;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.CitaMedica} entity. This class is used
 * in {@link ni.edu.mney.web.rest.CitaMedicaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /cita-medicas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CitaMedicaCriteria implements Serializable, Criteria {

    /**
     * Class for filtering EstadoCita
     */
    public static class EstadoCitaFilter extends Filter<EstadoCita> {

        public EstadoCitaFilter() {}

        public EstadoCitaFilter(EstadoCitaFilter filter) {
            super(filter);
        }

        @Override
        public EstadoCitaFilter copy() {
            return new EstadoCitaFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private ZonedDateTimeFilter fechaHora;

    private EstadoCitaFilter estado;

    private StringFilter observaciones;

    private StringFilter userId;

    private LongFilter pacienteId;

    private Boolean distinct;

    public CitaMedicaCriteria() {}

    public CitaMedicaCriteria(CitaMedicaCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.fechaHora = other.optionalFechaHora().map(ZonedDateTimeFilter::copy).orElse(null);
        this.estado = other.optionalEstado().map(EstadoCitaFilter::copy).orElse(null);
        this.observaciones = other.optionalObservaciones().map(StringFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(StringFilter::copy).orElse(null);
        this.pacienteId = other.optionalPacienteId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CitaMedicaCriteria copy() {
        return new CitaMedicaCriteria(this);
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

    public ZonedDateTimeFilter getFechaHora() {
        return fechaHora;
    }

    public Optional<ZonedDateTimeFilter> optionalFechaHora() {
        return Optional.ofNullable(fechaHora);
    }

    public ZonedDateTimeFilter fechaHora() {
        if (fechaHora == null) {
            setFechaHora(new ZonedDateTimeFilter());
        }
        return fechaHora;
    }

    public void setFechaHora(ZonedDateTimeFilter fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoCitaFilter getEstado() {
        return estado;
    }

    public Optional<EstadoCitaFilter> optionalEstado() {
        return Optional.ofNullable(estado);
    }

    public EstadoCitaFilter estado() {
        if (estado == null) {
            setEstado(new EstadoCitaFilter());
        }
        return estado;
    }

    public void setEstado(EstadoCitaFilter estado) {
        this.estado = estado;
    }

    public StringFilter getObservaciones() {
        return observaciones;
    }

    public Optional<StringFilter> optionalObservaciones() {
        return Optional.ofNullable(observaciones);
    }

    public StringFilter observaciones() {
        if (observaciones == null) {
            setObservaciones(new StringFilter());
        }
        return observaciones;
    }

    public void setObservaciones(StringFilter observaciones) {
        this.observaciones = observaciones;
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

    public LongFilter getPacienteId() {
        return pacienteId;
    }

    public Optional<LongFilter> optionalPacienteId() {
        return Optional.ofNullable(pacienteId);
    }

    public LongFilter pacienteId() {
        if (pacienteId == null) {
            setPacienteId(new LongFilter());
        }
        return pacienteId;
    }

    public void setPacienteId(LongFilter pacienteId) {
        this.pacienteId = pacienteId;
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
        final CitaMedicaCriteria that = (CitaMedicaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(fechaHora, that.fechaHora) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(observaciones, that.observaciones) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(pacienteId, that.pacienteId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fechaHora, estado, observaciones, userId, pacienteId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CitaMedicaCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalFechaHora().map(f -> "fechaHora=" + f + ", ").orElse("") +
            optionalEstado().map(f -> "estado=" + f + ", ").orElse("") +
            optionalObservaciones().map(f -> "observaciones=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalPacienteId().map(f -> "pacienteId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
