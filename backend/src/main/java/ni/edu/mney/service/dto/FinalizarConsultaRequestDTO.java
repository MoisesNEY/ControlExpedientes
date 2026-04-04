package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO de solicitud para finalizar una consulta médica de forma transaccional.
 * Recibe todos los datos necesarios en un solo request para crear la
 * ConsultaMedica,
 * vincular el Diagnóstico Principal, crear las Recetas y cambiar el estado de
 * la Cita a ATENDIDA.
 */
public class FinalizarConsultaRequestDTO implements Serializable {

    /** Motivo de la consulta (viene de la cita/triage, confirmar o sobrescribir) */
    private String motivoConsulta;

    /** Notas médicas de la exploración y evolución */
    private String notasMedicas;

    /** ID del Diagnóstico principal seleccionado en el autocomplete CIE-10 */
    private Long diagnosticoPrincipalId;

    /** Lista de prescripciones para crear Recetas */
    private List<RecetaRequestDTO> recetas;

    /** DTO interno para cada línea de receta */
    public static class RecetaRequestDTO implements Serializable {
        private Long medicamentoId;
        private String dosis;
        private String frecuencia;
        private String duracion;
        private Integer cantidad;

        public Long getMedicamentoId() {
            return medicamentoId;
        }

        public void setMedicamentoId(Long medicamentoId) {
            this.medicamentoId = medicamentoId;
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

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
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

    public Long getDiagnosticoPrincipalId() {
        return diagnosticoPrincipalId;
    }

    public void setDiagnosticoPrincipalId(Long diagnosticoPrincipalId) {
        this.diagnosticoPrincipalId = diagnosticoPrincipalId;
    }

    public List<RecetaRequestDTO> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<RecetaRequestDTO> recetas) {
        this.recetas = recetas;
    }
}
