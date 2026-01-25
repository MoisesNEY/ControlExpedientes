package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.HistorialClinico} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HistorialClinicoDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime fechaRegistro;

    @NotNull
    private String descripcion;

    private ExpedienteClinicoDTO expediente;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(ZonedDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public ExpedienteClinicoDTO getExpediente() {
        return expediente;
    }

    public void setExpediente(ExpedienteClinicoDTO expediente) {
        this.expediente = expediente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistorialClinicoDTO)) {
            return false;
        }

        HistorialClinicoDTO historialClinicoDTO = (HistorialClinicoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, historialClinicoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HistorialClinicoDTO{" +
            "id=" + getId() +
            ", fechaRegistro='" + getFechaRegistro() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", expediente=" + getExpediente() +
            "}";
    }
}
