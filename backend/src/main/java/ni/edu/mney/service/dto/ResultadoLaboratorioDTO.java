package ni.edu.mney.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link ni.edu.mney.domain.ResultadoLaboratorio} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResultadoLaboratorioDTO implements Serializable {

    private Long id;

    @NotNull
    private String tipoExamen;

    @NotNull
    private String resultado;

    private String valorReferencia;

    private String unidad;

    private String observaciones;

    @NotNull
    private LocalDate fechaExamen;

    private PacienteDTO paciente;

    private ConsultaMedicaDTO consulta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoExamen() {
        return tipoExamen;
    }

    public void setTipoExamen(String tipoExamen) {
        this.tipoExamen = tipoExamen;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getValorReferencia() {
        return valorReferencia;
    }

    public void setValorReferencia(String valorReferencia) {
        this.valorReferencia = valorReferencia;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaExamen() {
        return fechaExamen;
    }

    public void setFechaExamen(LocalDate fechaExamen) {
        this.fechaExamen = fechaExamen;
    }

    public PacienteDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteDTO paciente) {
        this.paciente = paciente;
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
        if (!(o instanceof ResultadoLaboratorioDTO)) {
            return false;
        }

        ResultadoLaboratorioDTO resultadoLaboratorioDTO = (ResultadoLaboratorioDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, resultadoLaboratorioDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResultadoLaboratorioDTO{" +
            "id=" + getId() +
            ", tipoExamen='" + getTipoExamen() + "'" +
            ", resultado='" + getResultado() + "'" +
            ", valorReferencia='" + getValorReferencia() + "'" +
            ", unidad='" + getUnidad() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", fechaExamen='" + getFechaExamen() + "'" +
            ", paciente=" + getPaciente() +
            ", consulta=" + getConsulta() +
            "}";
    }
}
