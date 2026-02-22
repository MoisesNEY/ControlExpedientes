package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.time.LocalDate;
import ni.edu.mney.domain.enumeration.Sexo;

/**
 * A DTO for the {@link ni.edu.mney.domain.Paciente} entity, containing only
 * non-sensitive data for public/reception use.
 */
public class PacientePublicDTO implements Serializable {

    private Long id;
    private String codigo;
    private String nombres;
    private String apellidos;
    private Sexo sexo;
    private LocalDate fechaNacimiento;
    private Boolean activo;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "PacientePublicDTO{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", sexo=" + sexo +
                ", fechaNacimiento=" + fechaNacimiento +
                ", activo=" + activo +
                '}';
    }
}
