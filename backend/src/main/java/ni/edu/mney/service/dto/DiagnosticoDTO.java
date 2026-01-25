package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.Diagnostico} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DiagnosticoDTO implements Serializable {

    private Long id;

    @NotNull
    private String descripcion;

    private String codigoCIE;

    private ConsultaMedicaDTO consulta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigoCIE() {
        return codigoCIE;
    }

    public void setCodigoCIE(String codigoCIE) {
        this.codigoCIE = codigoCIE;
    }

    public ConsultaMedicaDTO getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaMedicaDTO consulta) {
        this.consulta = consulta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiagnosticoDTO)) {
            return false;
        }

        DiagnosticoDTO diagnosticoDTO = (DiagnosticoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, diagnosticoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DiagnosticoDTO{" +
            "id=" + getId() +
            ", descripcion='" + getDescripcion() + "'" +
            ", codigoCIE='" + getCodigoCIE() + "'" +
            ", consulta=" + getConsulta() +
            "}";
    }
}
