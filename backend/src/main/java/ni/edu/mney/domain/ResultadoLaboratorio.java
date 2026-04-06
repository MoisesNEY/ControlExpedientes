package ni.edu.mney.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ResultadoLaboratorio.
 */
@Entity
@Table(name = "resultado_laboratorio")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResultadoLaboratorio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "tipo_examen", nullable = false)
    private String tipoExamen;

    @NotNull
    @Column(name = "resultado", nullable = false)
    private String resultado;

    @Column(name = "valor_referencia")
    private String valorReferencia;

    @Column(name = "unidad")
    private String unidad;

    @Column(name = "observaciones")
    private String observaciones;

    @NotNull
    @Column(name = "fecha_examen", nullable = false)
    private LocalDate fechaExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "expediente", "citas" }, allowSetters = true)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "diagnosticos", "tratamientos", "signosVitales", "recetas", "user",
            "expediente" }, allowSetters = true)
    private ConsultaMedica consulta;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ResultadoLaboratorio id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoExamen() {
        return this.tipoExamen;
    }

    public ResultadoLaboratorio tipoExamen(String tipoExamen) {
        this.setTipoExamen(tipoExamen);
        return this;
    }

    public void setTipoExamen(String tipoExamen) {
        this.tipoExamen = tipoExamen;
    }

    public String getResultado() {
        return this.resultado;
    }

    public ResultadoLaboratorio resultado(String resultado) {
        this.setResultado(resultado);
        return this;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getValorReferencia() {
        return this.valorReferencia;
    }

    public ResultadoLaboratorio valorReferencia(String valorReferencia) {
        this.setValorReferencia(valorReferencia);
        return this;
    }

    public void setValorReferencia(String valorReferencia) {
        this.valorReferencia = valorReferencia;
    }

    public String getUnidad() {
        return this.unidad;
    }

    public ResultadoLaboratorio unidad(String unidad) {
        this.setUnidad(unidad);
        return this;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getObservaciones() {
        return this.observaciones;
    }

    public ResultadoLaboratorio observaciones(String observaciones) {
        this.setObservaciones(observaciones);
        return this;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaExamen() {
        return this.fechaExamen;
    }

    public ResultadoLaboratorio fechaExamen(LocalDate fechaExamen) {
        this.setFechaExamen(fechaExamen);
        return this;
    }

    public void setFechaExamen(LocalDate fechaExamen) {
        this.fechaExamen = fechaExamen;
    }

    public Paciente getPaciente() {
        return this.paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public ResultadoLaboratorio paciente(Paciente paciente) {
        this.setPaciente(paciente);
        return this;
    }

    public ConsultaMedica getConsulta() {
        return this.consulta;
    }

    public void setConsulta(ConsultaMedica consultaMedica) {
        this.consulta = consultaMedica;
    }

    public ResultadoLaboratorio consulta(ConsultaMedica consultaMedica) {
        this.setConsulta(consultaMedica);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResultadoLaboratorio)) {
            return false;
        }
        return getId() != null && getId().equals(((ResultadoLaboratorio) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResultadoLaboratorio{" +
            "id=" + getId() +
            ", tipoExamen='" + getTipoExamen() + "'" +
            ", resultado='" + getResultado() + "'" +
            ", valorReferencia='" + getValorReferencia() + "'" +
            ", unidad='" + getUnidad() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", fechaExamen='" + getFechaExamen() + "'" +
            "}";
    }
}
