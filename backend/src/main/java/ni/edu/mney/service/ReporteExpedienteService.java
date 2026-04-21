package ni.edu.mney.service;

import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.repository.PacienteRepository;
import ni.edu.mney.service.report.PdfReportSupport;
import ni.edu.mney.service.report.PdfReportSupport.InfoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de generar el PDF resumen del expediente clínico.
 */
@Service
@Transactional(readOnly = true)
public class ReporteExpedienteService {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteExpedienteService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ExpedienteClinicoRepository expedienteClinicoRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final PacienteRepository pacienteRepository;

    public ReporteExpedienteService(
        ExpedienteClinicoRepository expedienteClinicoRepository,
        ConsultaMedicaRepository consultaMedicaRepository,
        PacienteRepository pacienteRepository
    ) {
        this.expedienteClinicoRepository = expedienteClinicoRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Genera un PDF con el resumen del expediente clínico.
     *
     * @param expedienteId el ID del expediente clínico
     * @return el archivo PDF en bytes
     */
    public byte[] generarExpedientePdf(Long expedienteId) {
        LOG.debug("Request para generar expediente PDF para ExpedienteClinico ID: {}", expedienteId);

        ExpedienteClinico expediente = expedienteClinicoRepository.findById(expedienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el expediente clínico con ID: " + expedienteId));

        Paciente paciente = pacienteRepository.findByExpedienteId(expedienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente del expediente con ID: " + expedienteId));

        List<ConsultaMedica> allConsultas = consultaMedicaRepository
            .findByExpedienteIdOrderByFechaConsultaDesc(expedienteId);

        // Load details for up to the last 5 consultations
        List<ConsultaMedica> last5 = allConsultas.stream()
            .limit(5)
            .map(c -> consultaMedicaRepository.findOneWithDetailsById(c.getId()).orElse(c))
            .toList();

        long totalRecetas = last5.stream()
            .mapToLong(c -> c.getRecetas() != null ? c.getRecetas().size() : 0)
            .sum();
        // If there are more than 5 consultations, we don't have their details loaded,
        // so we report receta count only for the loaded ones as an approximation.
        // For a precise count we would need to query all, but this keeps performance optimal.

        return buildPdf(paciente, expediente, allConsultas, last5, totalRecetas);
    }

    private byte[] buildPdf(
        Paciente paciente,
        ExpedienteClinico expediente,
        List<ConsultaMedica> allConsultas,
        List<ConsultaMedica> last5,
        long totalRecetas
    ) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.lowagie.text.Document document = PdfReportSupport.newDocument();
            PdfWriter.getInstance(document, baos);
            document.open();

            PdfReportSupport.Fonts fonts = PdfReportSupport.fonts();
            PdfReportSupport.addHeader(
                document,
                fonts,
                "Expediente clínico",
                "Resumen consolidado del expediente del paciente",
                "Expediente",
                expediente.getNumeroExpediente(),
                LocalDate.now()
            );
            PdfReportSupport.addInfoGrid(document, fonts, List.of(
                new InfoItem("Paciente", paciente.getNombres() + " " + paciente.getApellidos()),
                new InfoItem("Cédula", paciente.getCedula() != null ? paciente.getCedula() : "N/D"),
                new InfoItem("Sexo", paciente.getSexo() != null ? paciente.getSexo().toString() : "N/D"),
                new InfoItem("Fecha de nacimiento", paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().format(DATE_FMT) : "N/D"),
                new InfoItem("Estado civil", paciente.getEstadoCivil() != null ? paciente.getEstadoCivil().toString() : "N/D"),
                new InfoItem("Teléfono", paciente.getTelefono() != null ? paciente.getTelefono() : "N/D"),
                new InfoItem("Dirección", paciente.getDireccion() != null ? paciente.getDireccion() : "N/D"),
                new InfoItem("Correo", paciente.getEmail() != null ? paciente.getEmail() : "N/D"),
                new InfoItem("Fecha de apertura", expediente.getFechaApertura() != null ? expediente.getFechaApertura().format(DATE_FMT) : "N/D"),
                new InfoItem("Observaciones", expediente.getObservaciones() != null && !expediente.getObservaciones().isBlank() ? expediente.getObservaciones() : "Sin observaciones")
            ));

            PdfReportSupport.addSectionTitle(document, fonts, "Resumen del expediente");
            document.add(new Paragraph("Total de consultas: " + allConsultas.size(), fonts.normal()));

            if (!allConsultas.isEmpty()) {
                ConsultaMedica ultimaConsulta = allConsultas.stream()
                    .max(Comparator.comparing(ConsultaMedica::getFechaConsulta))
                    .orElse(allConsultas.get(0));
                document.add(new Paragraph(
                    "Última consulta: " + ultimaConsulta.getFechaConsulta().format(DATE_FMT), fonts.normal()
                ));
            }
            document.add(new Paragraph("Total de recetas (últimas 5 consultas): " + totalRecetas, fonts.normal()));

            if (!last5.isEmpty()) {
                PdfReportSupport.addSectionTitle(document, fonts, "Últimas consultas");

                PdfPTable table = PdfReportSupport.createTable(
                    fonts,
                    new float[] { 1.5f, 3f, 2f, 2f },
                    "Fecha",
                    "Motivo de consulta",
                    "Doctor",
                    "Diagnósticos"
                );

                for (ConsultaMedica c : last5) {
                    table.addCell(PdfReportSupport.createBodyCell(c.getFechaConsulta().format(DATE_FMT), fonts.normal()));
                    table.addCell(PdfReportSupport.createBodyCell(c.getMotivoConsulta(), fonts.small()));

                    String doctor = c.getUser() != null ? c.getUser().getLogin() : "N/A";
                    table.addCell(PdfReportSupport.createBodyCell(doctor, fonts.small()));

                    StringBuilder diags = new StringBuilder();
                    if (c.getDiagnosticos() != null) {
                        c.getDiagnosticos().forEach(d -> {
                            if (!diags.isEmpty()) diags.append(", ");
                            if (d.getCodigoCIE() != null) {
                                diags.append("[").append(d.getCodigoCIE()).append("] ");
                            }
                            diags.append(d.getDescripcion());
                        });
                    }
                    table.addCell(PdfReportSupport.createBodyCell(diags.length() > 0 ? diags.toString() : "-", fonts.small()));
                }
                document.add(table);
            }

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | java.io.IOException e) {
            LOG.error("Error al generar el PDF del expediente clínico", e);
            throw new RuntimeException("Error al generar el PDF del expediente clínico", e);
        }
    }

}
