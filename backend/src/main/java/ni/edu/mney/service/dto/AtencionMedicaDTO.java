package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * View Model que agrupa todos los datos necesarios para finalizar una consulta
 * médica.
 */
public class AtencionMedicaDTO implements Serializable {

    private ConsultaMedicaDTO consulta;
    private SignosVitalesDTO signosVitales;
    private DiagnosticoDTO diagnostico;
    private List<RecetaDTO> recetas;

    public ConsultaMedicaDTO getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaMedicaDTO consulta) {
        this.consulta = consulta;
    }

    public SignosVitalesDTO getSignosVitales() {
        return signosVitales;
    }

    public void setSignosVitales(SignosVitalesDTO signosVitales) {
        this.signosVitales = signosVitales;
    }

    public DiagnosticoDTO getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(DiagnosticoDTO diagnostico) {
        this.diagnostico = diagnostico;
    }

    public List<RecetaDTO> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<RecetaDTO> recetas) {
        this.recetas = recetas;
    }

    @Override
    public String toString() {
        return "AtencionMedicaDTO{" +
                "consulta=" + consulta +
                ", signosVitales=" + signosVitales +
                ", diagnostico=" + diagnostico +
                ", recetas=" + recetas +
                '}';
    }
}
