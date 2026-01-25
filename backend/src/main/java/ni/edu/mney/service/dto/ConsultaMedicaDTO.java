package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.ConsultaMedica} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConsultaMedicaDTO implements Serializable {

    private Long id;

    @NotNull
    private LocalDate fechaConsulta;

    @NotNull
    private String motivoConsulta;

    private String notasMedicas;

    private UserDTO user;

    private ExpedienteClinicoDTO expediente;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getNotasMedicas() {
        return notasMedicas;
    }

    public void setNotasMedicas(String notasMedicas) {
        this.notasMedicas = notasMedicas;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
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
        if (!(o instanceof ConsultaMedicaDTO)) {
            return false;
        }

        ConsultaMedicaDTO consultaMedicaDTO = (ConsultaMedicaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, consultaMedicaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConsultaMedicaDTO{" +
            "id=" + getId() +
            ", fechaConsulta='" + getFechaConsulta() + "'" +
            ", motivoConsulta='" + getMotivoConsulta() + "'" +
            ", notasMedicas='" + getNotasMedicas() + "'" +
            ", user=" + getUser() +
            ", expediente=" + getExpediente() +
            "}";
    }
}
