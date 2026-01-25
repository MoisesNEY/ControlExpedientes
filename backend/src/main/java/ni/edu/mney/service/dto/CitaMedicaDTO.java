package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import ni.edu.mney.domain.enumeration.EstadoCita;

/**
 * A DTO for the {@link ni.edu.mney.domain.CitaMedica} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CitaMedicaDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime fechaHora;

    @NotNull
    private EstadoCita estado;

    private String observaciones;

    private UserDTO user;

    private PacienteDTO paciente;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(ZonedDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public PacienteDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteDTO paciente) {
        this.paciente = paciente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CitaMedicaDTO)) {
            return false;
        }

        CitaMedicaDTO citaMedicaDTO = (CitaMedicaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, citaMedicaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CitaMedicaDTO{" +
            "id=" + getId() +
            ", fechaHora='" + getFechaHora() + "'" +
            ", estado='" + getEstado() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", user=" + getUser() +
            ", paciente=" + getPaciente() +
            "}";
    }
}
