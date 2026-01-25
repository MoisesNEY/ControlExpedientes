package ni.edu.mney.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import ni.edu.mney.domain.enumeration.EstadoCivil;
import ni.edu.mney.domain.enumeration.Sexo;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ni.edu.mney.domain.Paciente} entity. This class is used
 * in {@link ni.edu.mney.web.rest.PacienteResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /pacientes?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PacienteCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Sexo
     */
    public static class SexoFilter extends Filter<Sexo> {

        public SexoFilter() {}

        public SexoFilter(SexoFilter filter) {
            super(filter);
        }

        @Override
        public SexoFilter copy() {
            return new SexoFilter(this);
        }
    }

    /**
     * Class for filtering EstadoCivil
     */
    public static class EstadoCivilFilter extends Filter<EstadoCivil> {

        public EstadoCivilFilter() {}

        public EstadoCivilFilter(EstadoCivilFilter filter) {
            super(filter);
        }

        @Override
        public EstadoCivilFilter copy() {
            return new EstadoCivilFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter codigo;

    private StringFilter nombres;

    private StringFilter apellidos;

    private SexoFilter sexo;

    private LocalDateFilter fechaNacimiento;

    private StringFilter cedula;

    private StringFilter telefono;

    private StringFilter direccion;

    private EstadoCivilFilter estadoCivil;

    private StringFilter email;

    private BooleanFilter activo;

    private LongFilter expedienteId;

    private LongFilter citaId;

    private Boolean distinct;

    public PacienteCriteria() {}

    public PacienteCriteria(PacienteCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.codigo = other.optionalCodigo().map(StringFilter::copy).orElse(null);
        this.nombres = other.optionalNombres().map(StringFilter::copy).orElse(null);
        this.apellidos = other.optionalApellidos().map(StringFilter::copy).orElse(null);
        this.sexo = other.optionalSexo().map(SexoFilter::copy).orElse(null);
        this.fechaNacimiento = other.optionalFechaNacimiento().map(LocalDateFilter::copy).orElse(null);
        this.cedula = other.optionalCedula().map(StringFilter::copy).orElse(null);
        this.telefono = other.optionalTelefono().map(StringFilter::copy).orElse(null);
        this.direccion = other.optionalDireccion().map(StringFilter::copy).orElse(null);
        this.estadoCivil = other.optionalEstadoCivil().map(EstadoCivilFilter::copy).orElse(null);
        this.email = other.optionalEmail().map(StringFilter::copy).orElse(null);
        this.activo = other.optionalActivo().map(BooleanFilter::copy).orElse(null);
        this.expedienteId = other.optionalExpedienteId().map(LongFilter::copy).orElse(null);
        this.citaId = other.optionalCitaId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public PacienteCriteria copy() {
        return new PacienteCriteria(this);
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

    public StringFilter getCodigo() {
        return codigo;
    }

    public Optional<StringFilter> optionalCodigo() {
        return Optional.ofNullable(codigo);
    }

    public StringFilter codigo() {
        if (codigo == null) {
            setCodigo(new StringFilter());
        }
        return codigo;
    }

    public void setCodigo(StringFilter codigo) {
        this.codigo = codigo;
    }

    public StringFilter getNombres() {
        return nombres;
    }

    public Optional<StringFilter> optionalNombres() {
        return Optional.ofNullable(nombres);
    }

    public StringFilter nombres() {
        if (nombres == null) {
            setNombres(new StringFilter());
        }
        return nombres;
    }

    public void setNombres(StringFilter nombres) {
        this.nombres = nombres;
    }

    public StringFilter getApellidos() {
        return apellidos;
    }

    public Optional<StringFilter> optionalApellidos() {
        return Optional.ofNullable(apellidos);
    }

    public StringFilter apellidos() {
        if (apellidos == null) {
            setApellidos(new StringFilter());
        }
        return apellidos;
    }

    public void setApellidos(StringFilter apellidos) {
        this.apellidos = apellidos;
    }

    public SexoFilter getSexo() {
        return sexo;
    }

    public Optional<SexoFilter> optionalSexo() {
        return Optional.ofNullable(sexo);
    }

    public SexoFilter sexo() {
        if (sexo == null) {
            setSexo(new SexoFilter());
        }
        return sexo;
    }

    public void setSexo(SexoFilter sexo) {
        this.sexo = sexo;
    }

    public LocalDateFilter getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Optional<LocalDateFilter> optionalFechaNacimiento() {
        return Optional.ofNullable(fechaNacimiento);
    }

    public LocalDateFilter fechaNacimiento() {
        if (fechaNacimiento == null) {
            setFechaNacimiento(new LocalDateFilter());
        }
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDateFilter fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public StringFilter getCedula() {
        return cedula;
    }

    public Optional<StringFilter> optionalCedula() {
        return Optional.ofNullable(cedula);
    }

    public StringFilter cedula() {
        if (cedula == null) {
            setCedula(new StringFilter());
        }
        return cedula;
    }

    public void setCedula(StringFilter cedula) {
        this.cedula = cedula;
    }

    public StringFilter getTelefono() {
        return telefono;
    }

    public Optional<StringFilter> optionalTelefono() {
        return Optional.ofNullable(telefono);
    }

    public StringFilter telefono() {
        if (telefono == null) {
            setTelefono(new StringFilter());
        }
        return telefono;
    }

    public void setTelefono(StringFilter telefono) {
        this.telefono = telefono;
    }

    public StringFilter getDireccion() {
        return direccion;
    }

    public Optional<StringFilter> optionalDireccion() {
        return Optional.ofNullable(direccion);
    }

    public StringFilter direccion() {
        if (direccion == null) {
            setDireccion(new StringFilter());
        }
        return direccion;
    }

    public void setDireccion(StringFilter direccion) {
        this.direccion = direccion;
    }

    public EstadoCivilFilter getEstadoCivil() {
        return estadoCivil;
    }

    public Optional<EstadoCivilFilter> optionalEstadoCivil() {
        return Optional.ofNullable(estadoCivil);
    }

    public EstadoCivilFilter estadoCivil() {
        if (estadoCivil == null) {
            setEstadoCivil(new EstadoCivilFilter());
        }
        return estadoCivil;
    }

    public void setEstadoCivil(EstadoCivilFilter estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public StringFilter getEmail() {
        return email;
    }

    public Optional<StringFilter> optionalEmail() {
        return Optional.ofNullable(email);
    }

    public StringFilter email() {
        if (email == null) {
            setEmail(new StringFilter());
        }
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public BooleanFilter getActivo() {
        return activo;
    }

    public Optional<BooleanFilter> optionalActivo() {
        return Optional.ofNullable(activo);
    }

    public BooleanFilter activo() {
        if (activo == null) {
            setActivo(new BooleanFilter());
        }
        return activo;
    }

    public void setActivo(BooleanFilter activo) {
        this.activo = activo;
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

    public LongFilter getCitaId() {
        return citaId;
    }

    public Optional<LongFilter> optionalCitaId() {
        return Optional.ofNullable(citaId);
    }

    public LongFilter citaId() {
        if (citaId == null) {
            setCitaId(new LongFilter());
        }
        return citaId;
    }

    public void setCitaId(LongFilter citaId) {
        this.citaId = citaId;
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
        final PacienteCriteria that = (PacienteCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(codigo, that.codigo) &&
            Objects.equals(nombres, that.nombres) &&
            Objects.equals(apellidos, that.apellidos) &&
            Objects.equals(sexo, that.sexo) &&
            Objects.equals(fechaNacimiento, that.fechaNacimiento) &&
            Objects.equals(cedula, that.cedula) &&
            Objects.equals(telefono, that.telefono) &&
            Objects.equals(direccion, that.direccion) &&
            Objects.equals(estadoCivil, that.estadoCivil) &&
            Objects.equals(email, that.email) &&
            Objects.equals(activo, that.activo) &&
            Objects.equals(expedienteId, that.expedienteId) &&
            Objects.equals(citaId, that.citaId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            codigo,
            nombres,
            apellidos,
            sexo,
            fechaNacimiento,
            cedula,
            telefono,
            direccion,
            estadoCivil,
            email,
            activo,
            expedienteId,
            citaId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PacienteCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalCodigo().map(f -> "codigo=" + f + ", ").orElse("") +
            optionalNombres().map(f -> "nombres=" + f + ", ").orElse("") +
            optionalApellidos().map(f -> "apellidos=" + f + ", ").orElse("") +
            optionalSexo().map(f -> "sexo=" + f + ", ").orElse("") +
            optionalFechaNacimiento().map(f -> "fechaNacimiento=" + f + ", ").orElse("") +
            optionalCedula().map(f -> "cedula=" + f + ", ").orElse("") +
            optionalTelefono().map(f -> "telefono=" + f + ", ").orElse("") +
            optionalDireccion().map(f -> "direccion=" + f + ", ").orElse("") +
            optionalEstadoCivil().map(f -> "estadoCivil=" + f + ", ").orElse("") +
            optionalEmail().map(f -> "email=" + f + ", ").orElse("") +
            optionalActivo().map(f -> "activo=" + f + ", ").orElse("") +
            optionalExpedienteId().map(f -> "expedienteId=" + f + ", ").orElse("") +
            optionalCitaId().map(f -> "citaId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
