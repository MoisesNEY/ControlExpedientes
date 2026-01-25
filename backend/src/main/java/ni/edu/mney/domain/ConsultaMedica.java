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
 * A ConsultaMedica.
 */
@Entity
@Table(name = "consulta_medica")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConsultaMedica implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "fecha_consulta", nullable = false)
    private LocalDate fechaConsulta;

    @NotNull
    @Column(name = "motivo_consulta", nullable = false)
    private String motivoConsulta;

    @Column(name = "notas_medicas")
    private String notasMedicas;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "consulta")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "consulta" }, allowSetters = true)
    private Set<Diagnostico> diagnosticos = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "consulta")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "consulta" }, allowSetters = true)
    private Set<Tratamiento> tratamientos = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "consulta")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "consulta" }, allowSetters = true)
    private Set<SignosVitales> signosVitales = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "consulta")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "medicamento", "consulta" }, allowSetters = true)
    private Set<Receta> recetas = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "consultas", "historials", "paciente" }, allowSetters = true)
    private ExpedienteClinico expediente;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ConsultaMedica id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaConsulta() {
        return this.fechaConsulta;
    }

    public ConsultaMedica fechaConsulta(LocalDate fechaConsulta) {
        this.setFechaConsulta(fechaConsulta);
        return this;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getMotivoConsulta() {
        return this.motivoConsulta;
    }

    public ConsultaMedica motivoConsulta(String motivoConsulta) {
        this.setMotivoConsulta(motivoConsulta);
        return this;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getNotasMedicas() {
        return this.notasMedicas;
    }

    public ConsultaMedica notasMedicas(String notasMedicas) {
        this.setNotasMedicas(notasMedicas);
        return this;
    }

    public void setNotasMedicas(String notasMedicas) {
        this.notasMedicas = notasMedicas;
    }

    public Set<Diagnostico> getDiagnosticos() {
        return this.diagnosticos;
    }

    public void setDiagnosticos(Set<Diagnostico> diagnosticos) {
        if (this.diagnosticos != null) {
            this.diagnosticos.forEach(i -> i.setConsulta(null));
        }
        if (diagnosticos != null) {
            diagnosticos.forEach(i -> i.setConsulta(this));
        }
        this.diagnosticos = diagnosticos;
    }

    public ConsultaMedica diagnosticos(Set<Diagnostico> diagnosticos) {
        this.setDiagnosticos(diagnosticos);
        return this;
    }

    public ConsultaMedica addDiagnostico(Diagnostico diagnostico) {
        this.diagnosticos.add(diagnostico);
        diagnostico.setConsulta(this);
        return this;
    }

    public ConsultaMedica removeDiagnostico(Diagnostico diagnostico) {
        this.diagnosticos.remove(diagnostico);
        diagnostico.setConsulta(null);
        return this;
    }

    public Set<Tratamiento> getTratamientos() {
        return this.tratamientos;
    }

    public void setTratamientos(Set<Tratamiento> tratamientos) {
        if (this.tratamientos != null) {
            this.tratamientos.forEach(i -> i.setConsulta(null));
        }
        if (tratamientos != null) {
            tratamientos.forEach(i -> i.setConsulta(this));
        }
        this.tratamientos = tratamientos;
    }

    public ConsultaMedica tratamientos(Set<Tratamiento> tratamientos) {
        this.setTratamientos(tratamientos);
        return this;
    }

    public ConsultaMedica addTratamiento(Tratamiento tratamiento) {
        this.tratamientos.add(tratamiento);
        tratamiento.setConsulta(this);
        return this;
    }

    public ConsultaMedica removeTratamiento(Tratamiento tratamiento) {
        this.tratamientos.remove(tratamiento);
        tratamiento.setConsulta(null);
        return this;
    }

    public Set<SignosVitales> getSignosVitales() {
        return this.signosVitales;
    }

    public void setSignosVitales(Set<SignosVitales> signosVitales) {
        if (this.signosVitales != null) {
            this.signosVitales.forEach(i -> i.setConsulta(null));
        }
        if (signosVitales != null) {
            signosVitales.forEach(i -> i.setConsulta(this));
        }
        this.signosVitales = signosVitales;
    }

    public ConsultaMedica signosVitales(Set<SignosVitales> signosVitales) {
        this.setSignosVitales(signosVitales);
        return this;
    }

    public ConsultaMedica addSignosVitales(SignosVitales signosVitales) {
        this.signosVitales.add(signosVitales);
        signosVitales.setConsulta(this);
        return this;
    }

    public ConsultaMedica removeSignosVitales(SignosVitales signosVitales) {
        this.signosVitales.remove(signosVitales);
        signosVitales.setConsulta(null);
        return this;
    }

    public Set<Receta> getRecetas() {
        return this.recetas;
    }

    public void setRecetas(Set<Receta> recetas) {
        if (this.recetas != null) {
            this.recetas.forEach(i -> i.setConsulta(null));
        }
        if (recetas != null) {
            recetas.forEach(i -> i.setConsulta(this));
        }
        this.recetas = recetas;
    }

    public ConsultaMedica recetas(Set<Receta> recetas) {
        this.setRecetas(recetas);
        return this;
    }

    public ConsultaMedica addReceta(Receta receta) {
        this.recetas.add(receta);
        receta.setConsulta(this);
        return this;
    }

    public ConsultaMedica removeReceta(Receta receta) {
        this.recetas.remove(receta);
        receta.setConsulta(null);
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ConsultaMedica user(User user) {
        this.setUser(user);
        return this;
    }

    public ExpedienteClinico getExpediente() {
        return this.expediente;
    }

    public void setExpediente(ExpedienteClinico expedienteClinico) {
        this.expediente = expedienteClinico;
    }

    public ConsultaMedica expediente(ExpedienteClinico expedienteClinico) {
        this.setExpediente(expedienteClinico);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsultaMedica)) {
            return false;
        }
        return getId() != null && getId().equals(((ConsultaMedica) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConsultaMedica{" +
            "id=" + getId() +
            ", fechaConsulta='" + getFechaConsulta() + "'" +
            ", motivoConsulta='" + getMotivoConsulta() + "'" +
            ", notasMedicas='" + getNotasMedicas() + "'" +
            "}";
    }
}
