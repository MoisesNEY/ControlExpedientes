package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.AuditoriaAcciones} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditoriaAccionesDTO implements Serializable {

    private Long id;

    @NotNull
    private String entidad;

    @NotNull
    private String accion;

    @NotNull
    private ZonedDateTime fecha;

    private String descripcion;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public ZonedDateTime getFecha() {
        return fecha;
    }

    public void setFecha(ZonedDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditoriaAccionesDTO)) {
            return false;
        }

        AuditoriaAccionesDTO auditoriaAccionesDTO = (AuditoriaAccionesDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, auditoriaAccionesDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditoriaAccionesDTO{" +
            "id=" + getId() +
            ", entidad='" + getEntidad() + "'" +
            ", accion='" + getAccion() + "'" +
            ", fecha='" + getFecha() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
