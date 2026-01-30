package ni.edu.mney.service;

import java.util.List;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.dto.DiagnosticoDTO;
import ni.edu.mney.service.dto.RecetaDTO;
import ni.edu.mney.service.dto.SignosVitalesDTO;
import ni.edu.mney.web.rest.vm.AtencionMedicaVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para orquestar el acto clínico completo.
 */
@Service
@Transactional
public class AtencionMedicaService {

    private static final Logger LOG = LoggerFactory.getLogger(AtencionMedicaService.class);

    private final ConsultaMedicaService consultaMedicaService;
    private final SignosVitalesService signosVitalesService;
    private final DiagnosticoService diagnosticoService;
    private final RecetaService recetaService;

    public AtencionMedicaService(
            ConsultaMedicaService consultaMedicaService,
            SignosVitalesService signosVitalesService,
            DiagnosticoService diagnosticoService,
            RecetaService recetaService) {
        this.consultaMedicaService = consultaMedicaService;
        this.signosVitalesService = signosVitalesService;
        this.diagnosticoService = diagnosticoService;
        this.recetaService = recetaService;
    }

    /**
     * Finaliza una consulta médica guardando de forma atómica la consulta, signos
     * vitales,
     * diagnóstico y recetas.
     * 
     * @param vm el view model con todos los datos de la atención.
     * @return la consulta médica guardada.
     */
    public ConsultaMedicaDTO finalizarConsulta(AtencionMedicaVM vm) {
        LOG.debug("Request to finalizar consulta médica de forma atómica");

        // 1. Guardar la Consulta Médica
        ConsultaMedicaDTO consultaDTO = consultaMedicaService.save(vm.getConsulta());

        // 2. Guardar Signos Vitales si existen
        if (vm.getSignosVitales() != null) {
            SignosVitalesDTO svDTO = vm.getSignosVitales();
            svDTO.setConsulta(consultaDTO);
            signosVitalesService.save(svDTO);
        }

        // 3. Guardar Diagnóstico si existe
        if (vm.getDiagnostico() != null) {
            DiagnosticoDTO diagDTO = vm.getDiagnostico();
            diagDTO.setConsulta(consultaDTO);
            diagnosticoService.save(diagDTO);
        }

        // 4. Guardar Recetas si existen
        if (vm.getRecetas() != null && !vm.getRecetas().isEmpty()) {
            for (RecetaDTO recetaDTO : vm.getRecetas()) {
                recetaDTO.setConsulta(consultaDTO);
                recetaService.save(recetaDTO);
            }
        }

        return consultaDTO;
    }
}
