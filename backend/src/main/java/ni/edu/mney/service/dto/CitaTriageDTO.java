package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import ni.edu.mney.domain.enumeration.EstadoCita;

/**
 * Custom DTO para unificar CitaMedica, Paciente y Signos Vitales (Triage)
 */
public class CitaTriageDTO implements Serializable {
    private Long citaId;
    private ZonedDateTime fechaHoraCita;
    private EstadoCita estadoCita;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    
    // Triage / ConsultaMedica Data
    private Long consultaId;
    private String motivoConsulta; 
    
    // Signos Vitales
    private Long signosVitalesId;
    private Double peso;
    private String presionArterial;
    private Double temperatura;

    public CitaTriageDTO() {}

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public ZonedDateTime getFechaHoraCita() {
        return fechaHoraCita;
    }

    public void setFechaHoraCita(ZonedDateTime fechaHoraCita) {
        this.fechaHoraCita = fechaHoraCita;
    }

    public EstadoCita getEstadoCita() {
        return estadoCita;
    }

    public void setEstadoCita(EstadoCita estadoCita) {
        this.estadoCita = estadoCita;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getPacienteNombreCompleto() {
        return pacienteNombreCompleto;
    }

    public void setPacienteNombreCompleto(String pacienteNombreCompleto) {
        this.pacienteNombreCompleto = pacienteNombreCompleto;
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public Long getSignosVitalesId() {
        return signosVitalesId;
    }

    public void setSignosVitalesId(Long signosVitalesId) {
        this.signosVitalesId = signosVitalesId;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getPresionArterial() {
        return presionArterial;
    }

    public void setPresionArterial(String presionArterial) {
        this.presionArterial = presionArterial;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }
}
