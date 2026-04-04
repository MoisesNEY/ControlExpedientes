package ni.edu.mney.web.rest;

import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.ReporteRecetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller para generar y devolver reportes PDF.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteResource.class);

    private final ReporteRecetaService reporteRecetaService;

    public ReporteResource(ReporteRecetaService reporteRecetaService) {
        this.reporteRecetaService = reporteRecetaService;
    }

    /**
     * {@code GET  /receta/{citaId}} : Genera el PDF de la receta asociada a la cita del día.
     *
     * @param citaId el ID de la CitaMedica.
     * @return el archivo PDF con estado {@code 200 (OK)}.
     */
    @GetMapping(value = "/receta/{citaId}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<byte[]> obtenerRecetaPdf(@PathVariable("citaId") Long citaId) {
        LOG.debug("REST request para obtener PDF de receta, CitaMedica ID: {}", citaId);

        byte[] pdfBytes = reporteRecetaService.generarRecetaPdf(citaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "receta-cita-" + citaId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
