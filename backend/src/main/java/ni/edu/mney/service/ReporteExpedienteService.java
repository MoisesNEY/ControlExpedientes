package ni.edu.mney.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
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
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // --- Header ---
            Paragraph title = new Paragraph("Ministerio de Salud - Expediente Clínico", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // --- Patient Information ---
            document.add(new Paragraph("Información del Paciente", subheaderFont));
            document.add(new Paragraph(" ", smallFont));
            document.add(new Paragraph("Nombre completo: " + paciente.getNombres() + " " + paciente.getApellidos(), normalFont));
            if (paciente.getCedula() != null) {
                document.add(new Paragraph("Cédula: " + paciente.getCedula(), normalFont));
            }
            document.add(new Paragraph("Sexo: " + (paciente.getSexo() != null ? paciente.getSexo() : "N/A"), normalFont));
            document.add(new Paragraph(
                "Fecha de Nacimiento: " + (paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().format(DATE_FMT) : "N/A"),
                normalFont
            ));
            document.add(new Paragraph("Estado Civil: " + (paciente.getEstadoCivil() != null ? paciente.getEstadoCivil() : "N/A"), normalFont));
            document.add(new Paragraph("Teléfono: " + (paciente.getTelefono() != null ? paciente.getTelefono() : "N/A"), normalFont));
            document.add(new Paragraph("Dirección: " + (paciente.getDireccion() != null ? paciente.getDireccion() : "N/A"), normalFont));
            document.add(new Paragraph("Email: " + (paciente.getEmail() != null ? paciente.getEmail() : "N/A"), normalFont));

            // --- Expediente Information ---
            document.add(new Paragraph(" ", normalFont));
            addSeparatorLine(document);
            document.add(new Paragraph("Datos del Expediente", subheaderFont));
            document.add(new Paragraph(" ", smallFont));
            document.add(new Paragraph("Número de Expediente: " + expediente.getNumeroExpediente(), normalFont));
            document.add(new Paragraph(
                "Fecha de Apertura: " + (expediente.getFechaApertura() != null ? expediente.getFechaApertura().format(DATE_FMT) : "N/A"),
                normalFont
            ));
            if (expediente.getObservaciones() != null && !expediente.getObservaciones().isBlank()) {
                document.add(new Paragraph("Observaciones: " + expediente.getObservaciones(), normalFont));
            }

            // --- Summary Statistics ---
            document.add(new Paragraph(" ", normalFont));
            addSeparatorLine(document);
            document.add(new Paragraph("Estadísticas del Expediente", subheaderFont));
            document.add(new Paragraph(" ", smallFont));
            document.add(new Paragraph("Total de consultas: " + allConsultas.size(), normalFont));

            if (!allConsultas.isEmpty()) {
                ConsultaMedica ultimaConsulta = allConsultas.stream()
                    .max(Comparator.comparing(ConsultaMedica::getFechaConsulta))
                    .orElse(allConsultas.get(0));
                document.add(new Paragraph(
                    "Última consulta: " + ultimaConsulta.getFechaConsulta().format(DATE_FMT), normalFont
                ));
            }
            document.add(new Paragraph("Total de recetas (últimas 5 consultas): " + totalRecetas, normalFont));

            // --- Last 5 Consultations ---
            if (!last5.isEmpty()) {
                document.add(new Paragraph(" ", normalFont));
                addSeparatorLine(document);
                document.add(new Paragraph("Últimas Consultas", subheaderFont));
                document.add(new Paragraph(" ", smallFont));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 1.5f, 3f, 2f, 2f });

                String[] headers = { "Fecha", "Motivo de Consulta", "Doctor", "Diagnósticos" };
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setPadding(6f);
                    table.addCell(cell);
                }

                for (ConsultaMedica c : last5) {
                    table.addCell(new Phrase(c.getFechaConsulta().format(DATE_FMT), normalFont));
                    table.addCell(new Phrase(c.getMotivoConsulta(), normalFont));

                    String doctor = c.getUser() != null ? c.getUser().getLogin() : "N/A";
                    table.addCell(new Phrase(doctor, normalFont));

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
                    table.addCell(new Phrase(diags.length() > 0 ? diags.toString() : "-", smallFont));
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

    private void addSeparatorLine(Document document) throws DocumentException {
        Paragraph line = new Paragraph(
            "─────────────────────────────────────────────────────────────────────────",
            FontFactory.getFont(FontFactory.HELVETICA, 8)
        );
        line.setSpacingAfter(5f);
        document.add(line);
    }
}
