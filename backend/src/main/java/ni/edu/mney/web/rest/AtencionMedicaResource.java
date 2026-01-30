package ni.edu.mney.web.rest;

import ni.edu.mney.service.AtencionMedicaService;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.web.rest.vm.AtencionMedicaVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller para gestionar el Acto Clínico (Atención Médica).
 */
@RestController
@RequestMapping("/api/atencion-medica")
public class AtencionMedicaResource {

    private static final Logger LOG = LoggerFactory.getLogger(AtencionMedicaResource.class);

    private final AtencionMedicaService atencionMedicaService;

    public AtencionMedicaResource(AtencionMedicaService atencionMedicaService) {
        this.atencionMedicaService = atencionMedicaService;
    }

    /**
     * {@code POST  /finalizar-consulta} : Finaliza una consulta médica completa.
     *
     * @param atencionMedicaVM el view model con los datos de la consulta, signos
     *                         vitales,
     *                         diagnóstico y recetas.
     * @return el {@link ResponseEntity} con estado {@code 200 (OK)} y con el cuerpo
     *         de la consulta médica guardada.
     */
    @PostMapping("/finalizar-consulta")
    public ResponseEntity<ConsultaMedicaDTO> finalizarConsulta(@RequestBody AtencionMedicaVM atencionMedicaVM) {
        LOG.debug("REST request to finalizar consulta médica : {}", atencionMedicaVM);
        ConsultaMedicaDTO result = atencionMedicaService.finalizarConsulta(atencionMedicaVM);
        return ResponseEntity.ok().body(result);
    }
}
