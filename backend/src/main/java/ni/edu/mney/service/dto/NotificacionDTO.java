package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO para notificaciones push vía WebSocket.
 */
public class NotificacionDTO implements Serializable {

    private String tipo; // "PACIENTE_LISTO", "CONSULTA_FINALIZADA", etc.
    private String mensaje;
    private Long citaId;
    private String pacienteNombre;
    private String medicoLogin;
    private ZonedDateTime timestamp;

    public NotificacionDTO() {
        this.timestamp = ZonedDateTime.now();
    }

    public NotificacionDTO(String tipo, String mensaje, Long citaId, String pacienteNombre) {
        this();
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.citaId = citaId;
        this.pacienteNombre = pacienteNombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public String getMedicoLogin() {
        return medicoLogin;
    }

    public void setMedicoLogin(String medicoLogin) {
        this.medicoLogin = medicoLogin;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NotificacionDTO{" +
                "tipo='" + tipo + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", citaId=" + citaId +
                ", pacienteNombre='" + pacienteNombre + '\'' +
                '}';
    }
}
