package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteRecetaPreviewRequestDTO implements Serializable {

    private Long citaId;
    private LocalDate fechaConsulta;
    private String nombrePaciente;
    private String codigoPaciente;
    private String cedulaPaciente;
    private String motivoConsulta;
    private String codigoDiagnostico;
    private String descripcionDiagnostico;
    private String notasMedicas;
    private String doctorName;
    private List<RecetaPreviewItemDTO> recetas = new ArrayList<>();

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public LocalDate getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getCodigoPaciente() {
        return codigoPaciente;
    }

    public void setCodigoPaciente(String codigoPaciente) {
        this.codigoPaciente = codigoPaciente;
    }

    public String getCedulaPaciente() {
        return cedulaPaciente;
    }

    public void setCedulaPaciente(String cedulaPaciente) {
        this.cedulaPaciente = cedulaPaciente;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getCodigoDiagnostico() {
        return codigoDiagnostico;
    }

    public void setCodigoDiagnostico(String codigoDiagnostico) {
        this.codigoDiagnostico = codigoDiagnostico;
    }

    public String getDescripcionDiagnostico() {
        return descripcionDiagnostico;
    }

    public void setDescripcionDiagnostico(String descripcionDiagnostico) {
        this.descripcionDiagnostico = descripcionDiagnostico;
    }

    public String getNotasMedicas() {
        return notasMedicas;
    }

    public void setNotasMedicas(String notasMedicas) {
        this.notasMedicas = notasMedicas;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public List<RecetaPreviewItemDTO> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<RecetaPreviewItemDTO> recetas) {
        this.recetas = recetas;
    }

    public static class RecetaPreviewItemDTO implements Serializable {

        private String medicamento;
        private String dosis;
        private String frecuencia;
        private String duracion;

        public String getMedicamento() {
            return medicamento;
        }

        public void setMedicamento(String medicamento) {
            this.medicamento = medicamento;
        }

        public String getDosis() {
            return dosis;
        }

        public void setDosis(String dosis) {
            this.dosis = dosis;
        }

        public String getFrecuencia() {
            return frecuencia;
        }

        public void setFrecuencia(String frecuencia) {
            this.frecuencia = frecuencia;
        }

        public String getDuracion() {
            return duracion;
        }

        public void setDuracion(String duracion) {
            this.duracion = duracion;
        }
    }
}
