package ni.edu.mney.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Receta.
 */
@Entity
@Table(name = "receta")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Receta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "dosis", nullable = false)
    private String dosis;

    @NotNull
    @Column(name = "frecuencia", nullable = false)
    private String frecuencia;

    @NotNull
    @Column(name = "duracion", nullable = false)
    private String duracion;

    @ManyToOne(fetch = FetchType.LAZY)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "diagnosticos", "tratamientos", "signosVitales", "recetas", "user", "expediente" }, allowSetters = true)
    private ConsultaMedica consulta;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Receta id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDosis() {
        return this.dosis;
    }

    public Receta dosis(String dosis) {
        this.setDosis(dosis);
        return this;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return this.frecuencia;
    }

    public Receta frecuencia(String frecuencia) {
        this.setFrecuencia(frecuencia);
        return this;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getDuracion() {
        return this.duracion;
    }

    public Receta duracion(String duracion) {
        this.setDuracion(duracion);
        return this;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public Medicamento getMedicamento() {
        return this.medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    public Receta medicamento(Medicamento medicamento) {
        this.setMedicamento(medicamento);
        return this;
    }

    public ConsultaMedica getConsulta() {
        return this.consulta;
    }

    public void setConsulta(ConsultaMedica consultaMedica) {
        this.consulta = consultaMedica;
    }

    public Receta consulta(ConsultaMedica consultaMedica) {
        this.setConsulta(consultaMedica);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Receta)) {
            return false;
        }
        return getId() != null && getId().equals(((Receta) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Receta{" +
            "id=" + getId() +
            ", dosis='" + getDosis() + "'" +
            ", frecuencia='" + getFrecuencia() + "'" +
            ", duracion='" + getDuracion() + "'" +
            "}";
    }
}
