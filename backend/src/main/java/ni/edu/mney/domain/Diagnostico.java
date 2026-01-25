package ni.edu.mney.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Diagnostico.
 */
@Entity
@Table(name = "diagnostico")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Diagnostico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "codigo_cie")
    private String codigoCIE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "diagnosticos", "tratamientos", "signosVitales", "recetas", "user", "expediente" }, allowSetters = true)
    private ConsultaMedica consulta;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Diagnostico id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public Diagnostico descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigoCIE() {
        return this.codigoCIE;
    }

    public Diagnostico codigoCIE(String codigoCIE) {
        this.setCodigoCIE(codigoCIE);
        return this;
    }

    public void setCodigoCIE(String codigoCIE) {
        this.codigoCIE = codigoCIE;
    }

    public ConsultaMedica getConsulta() {
        return this.consulta;
    }

    public void setConsulta(ConsultaMedica consultaMedica) {
        this.consulta = consultaMedica;
    }

    public Diagnostico consulta(ConsultaMedica consultaMedica) {
        this.setConsulta(consultaMedica);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Diagnostico)) {
            return false;
        }
        return getId() != null && getId().equals(((Diagnostico) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Diagnostico{" +
            "id=" + getId() +
            ", descripcion='" + getDescripcion() + "'" +
            ", codigoCIE='" + getCodigoCIE() + "'" +
            "}";
    }
}
