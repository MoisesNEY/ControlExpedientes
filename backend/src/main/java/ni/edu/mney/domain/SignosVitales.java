package ni.edu.mney.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * A SignosVitales.
 */
@Entity
@Audited
@Table(name = "signos_vitales")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignosVitales implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "peso")
    private Double peso;

    @Column(name = "altura")
    private Double altura;

    @Column(name = "presion_arterial")
    private String presionArterial;

    @Column(name = "temperatura")
    private Double temperatura;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "diagnosticos", "tratamientos", "signosVitales", "recetas", "user", "expediente" }, allowSetters = true)
    @NotAudited
    private ConsultaMedica consulta;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SignosVitales id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPeso() {
        return this.peso;
    }

    public SignosVitales peso(Double peso) {
        this.setPeso(peso);
        return this;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return this.altura;
    }

    public SignosVitales altura(Double altura) {
        this.setAltura(altura);
        return this;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public String getPresionArterial() {
        return this.presionArterial;
    }

    public SignosVitales presionArterial(String presionArterial) {
        this.setPresionArterial(presionArterial);
        return this;
    }

    public void setPresionArterial(String presionArterial) {
        this.presionArterial = presionArterial;
    }

    public Double getTemperatura() {
        return this.temperatura;
    }

    public SignosVitales temperatura(Double temperatura) {
        this.setTemperatura(temperatura);
        return this;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getFrecuenciaCardiaca() {
        return this.frecuenciaCardiaca;
    }

    public SignosVitales frecuenciaCardiaca(Integer frecuenciaCardiaca) {
        this.setFrecuenciaCardiaca(frecuenciaCardiaca);
        return this;
    }

    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }

    public ConsultaMedica getConsulta() {
        return this.consulta;
    }

    public void setConsulta(ConsultaMedica consultaMedica) {
        this.consulta = consultaMedica;
    }

    public SignosVitales consulta(ConsultaMedica consultaMedica) {
        this.setConsulta(consultaMedica);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignosVitales)) {
            return false;
        }
        return getId() != null && getId().equals(((SignosVitales) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignosVitales{" +
            "id=" + getId() +
            ", peso=" + getPeso() +
            ", altura=" + getAltura() +
            ", presionArterial='" + getPresionArterial() + "'" +
            ", temperatura=" + getTemperatura() +
            ", frecuenciaCardiaca=" + getFrecuenciaCardiaca() +
            "}";
    }
}
