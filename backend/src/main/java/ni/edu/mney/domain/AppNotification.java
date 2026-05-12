package ni.edu.mney.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;

@Entity
@Table(name = "app_notification")
public class AppNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Size(max = 60)
    @Column(name = "tipo", length = 60, nullable = false)
    private String tipo;

    @NotNull
    @Size(max = 500)
    @Column(name = "mensaje", length = 500, nullable = false)
    private String mensaje;

    @Column(name = "cita_id")
    private Long citaId;

    @Size(max = 180)
    @Column(name = "paciente_nombre", length = 180)
    private String pacienteNombre;

    @Size(max = 50)
    @Column(name = "medico_login", length = 50)
    private String medicoLogin;

    @Size(max = 180)
    @Column(name = "ruta_accion", length = 180)
    private String rutaAccion;

    @Size(max = 255)
    @Column(name = "archivo_descarga")
    private String archivoDescarga;

    @Size(max = 80)
    @Column(name = "accion_label", length = 80)
    private String accionLabel;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private ZonedDateTime timestamp = ZonedDateTime.now();

    @Column(name = "read_at")
    private Instant readAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getRutaAccion() {
        return rutaAccion;
    }

    public void setRutaAccion(String rutaAccion) {
        this.rutaAccion = rutaAccion;
    }

    public String getArchivoDescarga() {
        return archivoDescarga;
    }

    public void setArchivoDescarga(String archivoDescarga) {
        this.archivoDescarga = archivoDescarga;
    }

    public String getAccionLabel() {
        return accionLabel;
    }

    public void setAccionLabel(String accionLabel) {
        this.accionLabel = accionLabel;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
