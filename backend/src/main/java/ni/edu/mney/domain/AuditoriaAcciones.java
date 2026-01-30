package ni.edu.mney.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AuditoriaAcciones.
 */
@Entity
@Table(name = "auditoria_acciones")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditoriaAcciones implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "entidad", nullable = false)
    private String entidad;

    @NotNull
    @Column(name = "accion", nullable = false)
    private String accion;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private ZonedDateTime fecha;

    @Size(max = 1000)
    @Column(name = "descripcion")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AuditoriaAcciones id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntidad() {
        return this.entidad;
    }

    public AuditoriaAcciones entidad(String entidad) {
        this.setEntidad(entidad);
        return this;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getAccion() {
        return this.accion;
    }

    public AuditoriaAcciones accion(String accion) {
        this.setAccion(accion);
        return this;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public ZonedDateTime getFecha() {
        return this.fecha;
    }

    public AuditoriaAcciones fecha(ZonedDateTime fecha) {
        this.setFecha(fecha);
        return this;
    }

    public void setFecha(ZonedDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public AuditoriaAcciones descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AuditoriaAcciones user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and
    // setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditoriaAcciones)) {
            return false;
        }
        return getId() != null && getId().equals(((AuditoriaAcciones) o).getId());
    }

    @Override
    public int hashCode() {
        // see
        // https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditoriaAcciones{" +
                "id=" + getId() +
                ", entidad='" + getEntidad() + "'" +
                ", accion='" + getAccion() + "'" +
                ", fecha='" + getFecha() + "'" +
                ", descripcion='" + getDescripcion() + "'" +
                "}";
    }
}
