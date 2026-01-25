package ni.edu.mney.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Tratamiento.
 */
@Entity
@Table(name = "tratamiento")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Tratamiento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "indicaciones", nullable = false)
    private String indicaciones;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "diagnosticos", "tratamientos", "signosVitales", "recetas", "user", "expediente" }, allowSetters = true)
    private ConsultaMedica consulta;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Tratamiento id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndicaciones() {
        return this.indicaciones;
    }

    public Tratamiento indicaciones(String indicaciones) {
        this.setIndicaciones(indicaciones);
        return this;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public Integer getDuracionDias() {
        return this.duracionDias;
    }

    public Tratamiento duracionDias(Integer duracionDias) {
        this.setDuracionDias(duracionDias);
        return this;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    public ConsultaMedica getConsulta() {
        return this.consulta;
    }

    public void setConsulta(ConsultaMedica consultaMedica) {
        this.consulta = consultaMedica;
    }

    public Tratamiento consulta(ConsultaMedica consultaMedica) {
        this.setConsulta(consultaMedica);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tratamiento)) {
            return false;
        }
        return getId() != null && getId().equals(((Tratamiento) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Tratamiento{" +
            "id=" + getId() +
            ", indicaciones='" + getIndicaciones() + "'" +
            ", duracionDias=" + getDuracionDias() +
            "}";
    }
}
