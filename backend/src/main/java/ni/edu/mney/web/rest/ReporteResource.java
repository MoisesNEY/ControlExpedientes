package ni.edu.mney.web.rest;

import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.ReporteExpedienteService;
import ni.edu.mney.service.ReporteHistorialService;
import ni.edu.mney.service.ReporteRecetaService;
import ni.edu.mney.service.dto.ReporteRecetaPreviewRequestDTO;
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
    private final ReporteHistorialService reporteHistorialService;
    private final ReporteExpedienteService reporteExpedienteService;

    public ReporteResource(
        ReporteRecetaService reporteRecetaService,
        ReporteHistorialService reporteHistorialService,
        ReporteExpedienteService reporteExpedienteService
    ) {
        this.reporteRecetaService = reporteRecetaService;
        this.reporteHistorialService = reporteHistorialService;
        this.reporteExpedienteService = reporteExpedienteService;
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

    @GetMapping(value = "/receta/consulta/{consultaId}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<byte[]> obtenerRecetaPdfPorConsulta(@PathVariable("consultaId") Long consultaId) {
        LOG.debug("REST request para obtener PDF de receta, ConsultaMedica ID: {}", consultaId);

        byte[] pdfBytes = reporteRecetaService.generarRecetaPdfPorConsulta(consultaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "receta-consulta-" + consultaId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping(value = "/receta/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<byte[]> obtenerRecetaPreviewPdf(@RequestBody ReporteRecetaPreviewRequestDTO request) {
        LOG.debug("REST request para obtener PDF preliminar de receta, CitaMedica ID: {}", request.getCitaId());

        byte[] pdfBytes = reporteRecetaService.generarRecetaPreviewPdf(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "inline",
                "receta-preliminar-" + (request.getCitaId() != null ? request.getCitaId() : "consulta") + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * {@code GET  /historial/{pacienteId}} : Genera PDF del historial clínico completo del paciente.
     *
     * @param pacienteId el ID del Paciente.
     * @return el archivo PDF con estado {@code 200 (OK)}.
     */
    @GetMapping(value = "/historial/{pacienteId}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<byte[]> obtenerHistorialPdf(@PathVariable("pacienteId") Long pacienteId) {
        LOG.debug("REST request para obtener PDF de historial clínico, Paciente ID: {}", pacienteId);

        byte[] pdfBytes = reporteHistorialService.generarHistorialPdf(pacienteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "historial-paciente-" + pacienteId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * {@code GET  /expediente/{expedienteId}} : Genera PDF del expediente clínico.
     *
     * @param expedienteId el ID del ExpedienteClinico.
     * @return el archivo PDF con estado {@code 200 (OK)}.
     */
    @GetMapping(value = "/expediente/{expedienteId}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<byte[]> obtenerExpedientePdf(@PathVariable("expedienteId") Long expedienteId) {
        LOG.debug("REST request para obtener PDF de expediente clínico, ExpedienteClinico ID: {}", expedienteId);

        byte[] pdfBytes = reporteExpedienteService.generarExpedientePdf(expedienteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "expediente-" + expedienteId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
