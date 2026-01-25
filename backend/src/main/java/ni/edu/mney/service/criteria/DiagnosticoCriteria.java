package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.Diagnostico} entity. This class is used
 * in {@link ni.edu.mney.web.rest.DiagnosticoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /diagnosticos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DiagnosticoCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter descripcion;

    private StringFilter codigoCIE;

    private LongFilter consultaId;

    private Boolean distinct;

    public DiagnosticoCriteria() {}

    public DiagnosticoCriteria(DiagnosticoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.descripcion = other.optionalDescripcion().map(StringFilter::copy).orElse(null);
        this.codigoCIE = other.optionalCodigoCIE().map(StringFilter::copy).orElse(null);
        this.consultaId = other.optionalConsultaId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public DiagnosticoCriteria copy() {
        return new DiagnosticoCriteria(this);
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

    public StringFilter getCodigoCIE() {
        return codigoCIE;
    }

    public Optional<StringFilter> optionalCodigoCIE() {
        return Optional.ofNullable(codigoCIE);
    }

    public StringFilter codigoCIE() {
        if (codigoCIE == null) {
            setCodigoCIE(new StringFilter());
        }
        return codigoCIE;
    }

    public void setCodigoCIE(StringFilter codigoCIE) {
        this.codigoCIE = codigoCIE;
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
        final DiagnosticoCriteria that = (DiagnosticoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(descripcion, that.descripcion) &&
            Objects.equals(codigoCIE, that.codigoCIE) &&
            Objects.equals(consultaId, that.consultaId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, descripcion, codigoCIE, consultaId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DiagnosticoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDescripcion().map(f -> "descripcion=" + f + ", ").orElse("") +
            optionalCodigoCIE().map(f -> "codigoCIE=" + f + ", ").orElse("") +
            optionalConsultaId().map(f -> "consultaId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
