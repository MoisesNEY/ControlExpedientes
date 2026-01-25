package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.ConsultaMedica} entity. This class is used
 * in {@link ni.edu.mney.web.rest.ConsultaMedicaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /consulta-medicas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConsultaMedicaCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LocalDateFilter fechaConsulta;

    private StringFilter motivoConsulta;

    private StringFilter notasMedicas;

    private LongFilter diagnosticoId;

    private LongFilter tratamientoId;

    private LongFilter signosVitalesId;

    private LongFilter recetaId;

    private StringFilter userId;

    private LongFilter expedienteId;

    private Boolean distinct;

    public ConsultaMedicaCriteria() {}

    public ConsultaMedicaCriteria(ConsultaMedicaCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.fechaConsulta = other.optionalFechaConsulta().map(LocalDateFilter::copy).orElse(null);
        this.motivoConsulta = other.optionalMotivoConsulta().map(StringFilter::copy).orElse(null);
        this.notasMedicas = other.optionalNotasMedicas().map(StringFilter::copy).orElse(null);
        this.diagnosticoId = other.optionalDiagnosticoId().map(LongFilter::copy).orElse(null);
        this.tratamientoId = other.optionalTratamientoId().map(LongFilter::copy).orElse(null);
        this.signosVitalesId = other.optionalSignosVitalesId().map(LongFilter::copy).orElse(null);
        this.recetaId = other.optionalRecetaId().map(LongFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(StringFilter::copy).orElse(null);
        this.expedienteId = other.optionalExpedienteId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ConsultaMedicaCriteria copy() {
        return new ConsultaMedicaCriteria(this);
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

    public LocalDateFilter getFechaConsulta() {
        return fechaConsulta;
    }

    public Optional<LocalDateFilter> optionalFechaConsulta() {
        return Optional.ofNullable(fechaConsulta);
    }

    public LocalDateFilter fechaConsulta() {
        if (fechaConsulta == null) {
            setFechaConsulta(new LocalDateFilter());
        }
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDateFilter fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public StringFilter getMotivoConsulta() {
        return motivoConsulta;
    }

    public Optional<StringFilter> optionalMotivoConsulta() {
        return Optional.ofNullable(motivoConsulta);
    }

    public StringFilter motivoConsulta() {
        if (motivoConsulta == null) {
            setMotivoConsulta(new StringFilter());
        }
        return motivoConsulta;
    }

    public void setMotivoConsulta(StringFilter motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public StringFilter getNotasMedicas() {
        return notasMedicas;
    }

    public Optional<StringFilter> optionalNotasMedicas() {
        return Optional.ofNullable(notasMedicas);
    }

    public StringFilter notasMedicas() {
        if (notasMedicas == null) {
            setNotasMedicas(new StringFilter());
        }
        return notasMedicas;
    }

    public void setNotasMedicas(StringFilter notasMedicas) {
        this.notasMedicas = notasMedicas;
    }

    public LongFilter getDiagnosticoId() {
        return diagnosticoId;
    }

    public Optional<LongFilter> optionalDiagnosticoId() {
        return Optional.ofNullable(diagnosticoId);
    }

    public LongFilter diagnosticoId() {
        if (diagnosticoId == null) {
            setDiagnosticoId(new LongFilter());
        }
        return diagnosticoId;
    }

    public void setDiagnosticoId(LongFilter diagnosticoId) {
        this.diagnosticoId = diagnosticoId;
    }

    public LongFilter getTratamientoId() {
        return tratamientoId;
    }

    public Optional<LongFilter> optionalTratamientoId() {
        return Optional.ofNullable(tratamientoId);
    }

    public LongFilter tratamientoId() {
        if (tratamientoId == null) {
            setTratamientoId(new LongFilter());
        }
        return tratamientoId;
    }

    public void setTratamientoId(LongFilter tratamientoId) {
        this.tratamientoId = tratamientoId;
    }

    public LongFilter getSignosVitalesId() {
        return signosVitalesId;
    }

    public Optional<LongFilter> optionalSignosVitalesId() {
        return Optional.ofNullable(signosVitalesId);
    }

    public LongFilter signosVitalesId() {
        if (signosVitalesId == null) {
            setSignosVitalesId(new LongFilter());
        }
        return signosVitalesId;
    }

    public void setSignosVitalesId(LongFilter signosVitalesId) {
        this.signosVitalesId = signosVitalesId;
    }

    public LongFilter getRecetaId() {
        return recetaId;
    }

    public Optional<LongFilter> optionalRecetaId() {
        return Optional.ofNullable(recetaId);
    }

    public LongFilter recetaId() {
        if (recetaId == null) {
            setRecetaId(new LongFilter());
        }
        return recetaId;
    }

    public void setRecetaId(LongFilter recetaId) {
        this.recetaId = recetaId;
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
        final ConsultaMedicaCriteria that = (ConsultaMedicaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(fechaConsulta, that.fechaConsulta) &&
            Objects.equals(motivoConsulta, that.motivoConsulta) &&
            Objects.equals(notasMedicas, that.notasMedicas) &&
            Objects.equals(diagnosticoId, that.diagnosticoId) &&
            Objects.equals(tratamientoId, that.tratamientoId) &&
            Objects.equals(signosVitalesId, that.signosVitalesId) &&
            Objects.equals(recetaId, that.recetaId) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(expedienteId, that.expedienteId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            fechaConsulta,
            motivoConsulta,
            notasMedicas,
            diagnosticoId,
            tratamientoId,
            signosVitalesId,
            recetaId,
            userId,
            expedienteId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConsultaMedicaCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalFechaConsulta().map(f -> "fechaConsulta=" + f + ", ").orElse("") +
            optionalMotivoConsulta().map(f -> "motivoConsulta=" + f + ", ").orElse("") +
            optionalNotasMedicas().map(f -> "notasMedicas=" + f + ", ").orElse("") +
            optionalDiagnosticoId().map(f -> "diagnosticoId=" + f + ", ").orElse("") +
            optionalTratamientoId().map(f -> "tratamientoId=" + f + ", ").orElse("") +
            optionalSignosVitalesId().map(f -> "signosVitalesId=" + f + ", ").orElse("") +
            optionalRecetaId().map(f -> "recetaId=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalExpedienteId().map(f -> "expedienteId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
