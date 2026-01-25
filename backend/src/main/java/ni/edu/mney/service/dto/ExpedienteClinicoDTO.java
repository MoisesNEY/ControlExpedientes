package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.ExpedienteClinico} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExpedienteClinicoDTO implements Serializable {

    private Long id;

    @NotNull
    private String numeroExpediente;

    @NotNull
    private LocalDate fechaApertura;

    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDate fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExpedienteClinicoDTO)) {
            return false;
        }

        ExpedienteClinicoDTO expedienteClinicoDTO = (ExpedienteClinicoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, expedienteClinicoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExpedienteClinicoDTO{" +
            "id=" + getId() +
            ", numeroExpediente='" + getNumeroExpediente() + "'" +
            ", fechaApertura='" + getFechaApertura() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            "}";
    }
}
