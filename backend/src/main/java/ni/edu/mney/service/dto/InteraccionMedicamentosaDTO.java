package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.InteraccionMedicamentosa} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InteraccionMedicamentosaDTO implements Serializable {

    private Long id;

    @NotNull
    private MedicamentoDTO medicamentoA;

    @NotNull
    private MedicamentoDTO medicamentoB;

    @NotNull
    private String severidad;

    @NotNull
    private String descripcion;

    private String recomendacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicamentoDTO getMedicamentoA() {
        return medicamentoA;
    }

    public void setMedicamentoA(MedicamentoDTO medicamentoA) {
        this.medicamentoA = medicamentoA;
    }

    public MedicamentoDTO getMedicamentoB() {
        return medicamentoB;
    }

    public void setMedicamentoB(MedicamentoDTO medicamentoB) {
        this.medicamentoB = medicamentoB;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InteraccionMedicamentosaDTO)) {
            return false;
        }

        InteraccionMedicamentosaDTO interaccionMedicamentosaDTO = (InteraccionMedicamentosaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, interaccionMedicamentosaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InteraccionMedicamentosaDTO{" +
            "id=" + getId() +
            ", medicamentoA=" + getMedicamentoA() +
            ", medicamentoB=" + getMedicamentoB() +
            ", severidad='" + getSeveridad() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", recomendacion='" + getRecomendacion() + "'" +
            "}";
    }
}
