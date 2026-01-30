package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para representar una entrada en el timeline clínico del paciente.
 */
public class TimelineEntryDTO implements Serializable {

    private LocalDate fecha;
    private String profesional;
    private String motivo;
    private List<String> diagnosticos;
    private List<String> recetas;
    private SignosVitalesDTO signosVitales;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getProfesional() {
        return profesional;
    }

    public void setProfesional(String profesional) {
        this.profesional = profesional;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public List<String> getDiagnosticos() {
        return diagnosticos;
    }

    public void setDiagnosticos(List<String> diagnosticos) {
        this.diagnosticos = diagnosticos;
    }

    public List<String> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<String> recetas) {
        this.recetas = recetas;
    }

    public SignosVitalesDTO getSignosVitales() {
        return signosVitales;
    }

    public void setSignosVitales(SignosVitalesDTO signosVitales) {
        this.signosVitales = signosVitales;
    }
}
