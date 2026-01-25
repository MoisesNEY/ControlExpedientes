package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.SignosVitales} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignosVitalesDTO implements Serializable {

    private Long id;

    private Double peso;

    private Double altura;

    private String presionArterial;

    private Double temperatura;

    private Integer frecuenciaCardiaca;

    private ConsultaMedicaDTO consulta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public String getPresionArterial() {
        return presionArterial;
    }

    public void setPresionArterial(String presionArterial) {
        this.presionArterial = presionArterial;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }

    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }

    public ConsultaMedicaDTO getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaMedicaDTO consulta) {
        this.consulta = consulta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignosVitalesDTO)) {
            return false;
        }

        SignosVitalesDTO signosVitalesDTO = (SignosVitalesDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, signosVitalesDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignosVitalesDTO{" +
            "id=" + getId() +
            ", peso=" + getPeso() +
            ", altura=" + getAltura() +
            ", presionArterial='" + getPresionArterial() + "'" +
            ", temperatura=" + getTemperatura() +
            ", frecuenciaCardiaca=" + getFrecuenciaCardiaca() +
            ", consulta=" + getConsulta() +
            "}";
    }
}
