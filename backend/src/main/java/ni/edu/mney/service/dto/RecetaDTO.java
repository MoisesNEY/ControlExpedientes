package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.Receta} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RecetaDTO implements Serializable {

    private Long id;

    @NotNull
    private String dosis;

    @NotNull
    private String frecuencia;

    @NotNull
    private String duracion;

    @NotNull
    @Min(value = 1)
    private Integer cantidad;

    private MedicamentoDTO medicamento;

    private ConsultaMedicaDTO consulta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public MedicamentoDTO getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(MedicamentoDTO medicamento) {
        this.medicamento = medicamento;
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
        if (!(o instanceof RecetaDTO)) {
            return false;
        }

        RecetaDTO recetaDTO = (RecetaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, recetaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecetaDTO{" +
                "id=" + getId() +
                ", dosis='" + getDosis() + "'" +
                ", frecuencia='" + getFrecuencia() + "'" +
                ", duracion='" + getDuracion() + "'" +
                ", cantidad=" + getCantidad() +
                ", medicamento=" + getMedicamento() +
                ", consulta=" + getConsulta() +
                "}";
    }
}
