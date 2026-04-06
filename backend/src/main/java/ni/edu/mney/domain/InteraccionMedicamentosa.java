package ni.edu.mney.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A InteraccionMedicamentosa.
 */
@Entity
@Table(name = "interaccion_medicamentosa")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InteraccionMedicamentosa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_a_id", nullable = false)
    private Medicamento medicamentoA;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_b_id", nullable = false)
    private Medicamento medicamentoB;

    @NotNull
    @Column(name = "severidad", nullable = false)
    private String severidad;

    @NotNull
    @Column(name = "descripcion", length = 2000, nullable = false)
    private String descripcion;

    @Column(name = "recomendacion", length = 2000)
    private String recomendacion;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InteraccionMedicamentosa id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medicamento getMedicamentoA() {
        return this.medicamentoA;
    }

    public InteraccionMedicamentosa medicamentoA(Medicamento medicamentoA) {
        this.setMedicamentoA(medicamentoA);
        return this;
    }

    public void setMedicamentoA(Medicamento medicamentoA) {
        this.medicamentoA = medicamentoA;
    }

    public Medicamento getMedicamentoB() {
        return this.medicamentoB;
    }

    public InteraccionMedicamentosa medicamentoB(Medicamento medicamentoB) {
        this.setMedicamentoB(medicamentoB);
        return this;
    }

    public void setMedicamentoB(Medicamento medicamentoB) {
        this.medicamentoB = medicamentoB;
    }

    public String getSeveridad() {
        return this.severidad;
    }

    public InteraccionMedicamentosa severidad(String severidad) {
        this.setSeveridad(severidad);
        return this;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public InteraccionMedicamentosa descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRecomendacion() {
        return this.recomendacion;
    }

    public InteraccionMedicamentosa recomendacion(String recomendacion) {
        this.setRecomendacion(recomendacion);
        return this;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InteraccionMedicamentosa)) {
            return false;
        }
        return getId() != null && getId().equals(((InteraccionMedicamentosa) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InteraccionMedicamentosa{" +
            "id=" + getId() +
            ", severidad='" + getSeveridad() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", recomendacion='" + getRecomendacion() + "'" +
            "}";
    }
}
