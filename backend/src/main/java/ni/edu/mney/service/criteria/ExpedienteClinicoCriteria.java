package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.ExpedienteClinico} entity. This class is used
 * in {@link ni.edu.mney.web.rest.ExpedienteClinicoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /expediente-clinicos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExpedienteClinicoCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter numeroExpediente;

    private LocalDateFilter fechaApertura;

    private StringFilter observaciones;

    private LongFilter consultaId;

    private LongFilter historialId;

    private LongFilter pacienteId;

    private Boolean distinct;

    public ExpedienteClinicoCriteria() {}

    public ExpedienteClinicoCriteria(ExpedienteClinicoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.numeroExpediente = other.optionalNumeroExpediente().map(StringFilter::copy).orElse(null);
        this.fechaApertura = other.optionalFechaApertura().map(LocalDateFilter::copy).orElse(null);
        this.observaciones = other.optionalObservaciones().map(StringFilter::copy).orElse(null);
        this.consultaId = other.optionalConsultaId().map(LongFilter::copy).orElse(null);
        this.historialId = other.optionalHistorialId().map(LongFilter::copy).orElse(null);
        this.pacienteId = other.optionalPacienteId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ExpedienteClinicoCriteria copy() {
        return new ExpedienteClinicoCriteria(this);
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

    public StringFilter getNumeroExpediente() {
        return numeroExpediente;
    }

    public Optional<StringFilter> optionalNumeroExpediente() {
        return Optional.ofNullable(numeroExpediente);
    }

    public StringFilter numeroExpediente() {
        if (numeroExpediente == null) {
            setNumeroExpediente(new StringFilter());
        }
        return numeroExpediente;
    }

    public void setNumeroExpediente(StringFilter numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public LocalDateFilter getFechaApertura() {
        return fechaApertura;
    }

    public Optional<LocalDateFilter> optionalFechaApertura() {
        return Optional.ofNullable(fechaApertura);
    }

    public LocalDateFilter fechaApertura() {
        if (fechaApertura == null) {
            setFechaApertura(new LocalDateFilter());
        }
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateFilter fechaApertura) {
        this.fechaApertura = fechaApertura;
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

    public LongFilter getHistorialId() {
        return historialId;
    }

    public Optional<LongFilter> optionalHistorialId() {
        return Optional.ofNullable(historialId);
    }

    public LongFilter historialId() {
        if (historialId == null) {
            setHistorialId(new LongFilter());
        }
        return historialId;
    }

    public void setHistorialId(LongFilter historialId) {
        this.historialId = historialId;
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
        final ExpedienteClinicoCriteria that = (ExpedienteClinicoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(numeroExpediente, that.numeroExpediente) &&
            Objects.equals(fechaApertura, that.fechaApertura) &&
            Objects.equals(observaciones, that.observaciones) &&
            Objects.equals(consultaId, that.consultaId) &&
            Objects.equals(historialId, that.historialId) &&
            Objects.equals(pacienteId, that.pacienteId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroExpediente, fechaApertura, observaciones, consultaId, historialId, pacienteId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExpedienteClinicoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNumeroExpediente().map(f -> "numeroExpediente=" + f + ", ").orElse("") +
            optionalFechaApertura().map(f -> "fechaApertura=" + f + ", ").orElse("") +
            optionalObservaciones().map(f -> "observaciones=" + f + ", ").orElse("") +
            optionalConsultaId().map(f -> "consultaId=" + f + ", ").orElse("") +
            optionalHistorialId().map(f -> "historialId=" + f + ", ").orElse("") +
            optionalPacienteId().map(f -> "pacienteId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
