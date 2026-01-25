package ni.edu.mney.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ExpedienteClinico.
 */
@Entity
@Table(name = "expediente_clinico")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExpedienteClinico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "numero_expediente", nullable = false, unique = true)
    private String numeroExpediente;

    @NotNull
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fechaApertura;

    @Column(name = "observaciones")
    private String observaciones;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "expediente")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "diagnosticos", "tratamientos", "signosVitales", "recetas", "user", "expediente" }, allowSetters = true)
    private Set<ConsultaMedica> consultas = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "expediente")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "expediente" }, allowSetters = true)
    private Set<HistorialClinico> historials = new HashSet<>();

    @JsonIgnoreProperties(value = { "expediente", "citas" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "expediente")
    private Paciente paciente;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ExpedienteClinico id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroExpediente() {
        return this.numeroExpediente;
    }

    public ExpedienteClinico numeroExpediente(String numeroExpediente) {
        this.setNumeroExpediente(numeroExpediente);
        return this;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public LocalDate getFechaApertura() {
        return this.fechaApertura;
    }

    public ExpedienteClinico fechaApertura(LocalDate fechaApertura) {
        this.setFechaApertura(fechaApertura);
        return this;
    }

    public void setFechaApertura(LocalDate fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getObservaciones() {
        return this.observaciones;
    }

    public ExpedienteClinico observaciones(String observaciones) {
        this.setObservaciones(observaciones);
        return this;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Set<ConsultaMedica> getConsultas() {
        return this.consultas;
    }

    public void setConsultas(Set<ConsultaMedica> consultaMedicas) {
        if (this.consultas != null) {
            this.consultas.forEach(i -> i.setExpediente(null));
        }
        if (consultaMedicas != null) {
            consultaMedicas.forEach(i -> i.setExpediente(this));
        }
        this.consultas = consultaMedicas;
    }

    public ExpedienteClinico consultas(Set<ConsultaMedica> consultaMedicas) {
        this.setConsultas(consultaMedicas);
        return this;
    }

    public ExpedienteClinico addConsulta(ConsultaMedica consultaMedica) {
        this.consultas.add(consultaMedica);
        consultaMedica.setExpediente(this);
        return this;
    }

    public ExpedienteClinico removeConsulta(ConsultaMedica consultaMedica) {
        this.consultas.remove(consultaMedica);
        consultaMedica.setExpediente(null);
        return this;
    }

    public Set<HistorialClinico> getHistorials() {
        return this.historials;
    }

    public void setHistorials(Set<HistorialClinico> historialClinicos) {
        if (this.historials != null) {
            this.historials.forEach(i -> i.setExpediente(null));
        }
        if (historialClinicos != null) {
            historialClinicos.forEach(i -> i.setExpediente(this));
        }
        this.historials = historialClinicos;
    }

    public ExpedienteClinico historials(Set<HistorialClinico> historialClinicos) {
        this.setHistorials(historialClinicos);
        return this;
    }

    public ExpedienteClinico addHistorial(HistorialClinico historialClinico) {
        this.historials.add(historialClinico);
        historialClinico.setExpediente(this);
        return this;
    }

    public ExpedienteClinico removeHistorial(HistorialClinico historialClinico) {
        this.historials.remove(historialClinico);
        historialClinico.setExpediente(null);
        return this;
    }

    public Paciente getPaciente() {
        return this.paciente;
    }

    public void setPaciente(Paciente paciente) {
        if (this.paciente != null) {
            this.paciente.setExpediente(null);
        }
        if (paciente != null) {
            paciente.setExpediente(this);
        }
        this.paciente = paciente;
    }

    public ExpedienteClinico paciente(Paciente paciente) {
        this.setPaciente(paciente);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExpedienteClinico)) {
            return false;
        }
        return getId() != null && getId().equals(((ExpedienteClinico) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExpedienteClinico{" +
            "id=" + getId() +
            ", numeroExpediente='" + getNumeroExpediente() + "'" +
            ", fechaApertura='" + getFechaApertura() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            "}";
    }
}
