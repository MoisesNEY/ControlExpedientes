package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.Tratamiento} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TratamientoDTO implements Serializable {

    private Long id;

    @NotNull
    private String indicaciones;

    private Integer duracionDias;

    private ConsultaMedicaDTO consulta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
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
        if (!(o instanceof TratamientoDTO)) {
            return false;
        }

        TratamientoDTO tratamientoDTO = (TratamientoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, tratamientoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TratamientoDTO{" +
            "id=" + getId() +
            ", indicaciones='" + getIndicaciones() + "'" +
            ", duracionDias=" + getDuracionDias() +
            ", consulta=" + getConsulta() +
            "}";
    }
}
