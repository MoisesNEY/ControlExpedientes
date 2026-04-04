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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Diagnostico;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.Receta;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de generar reportes en PDF usando OpenPDF.
 * Resuelve la ConsultaMedica a partir del citaId (la cita del día)
 * siguiendo la cadena: CitaMedica → Paciente → Expediente → ConsultaMedica.
 */
@Service
@Transactional(readOnly = true)
public class ReporteRecetaService {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteRecetaService.class);

    private final CitaMedicaRepository citaMedicaRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final ExpedienteClinicoRepository expedienteClinicoRepository;

    public ReporteRecetaService(
            CitaMedicaRepository citaMedicaRepository,
            ConsultaMedicaRepository consultaMedicaRepository,
            ExpedienteClinicoRepository expedienteClinicoRepository) {
        this.citaMedicaRepository = citaMedicaRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.expedienteClinicoRepository = expedienteClinicoRepository;
    }

    /**
     * Genera un array de bytes que representa la Receta Médica en formato PDF.
     * Recibe el citaId (CitaMedica) y busca la ConsultaMedica asociada.
     *
     * @param citaId El ID de la CitaMedica (cita del día)
     * @return El archivo PDF en bytes
     */
    public byte[] generarRecetaPdf(Long citaId) {
        LOG.debug("Request para generar receta PDF para CitaMedica ID: {}", citaId);

        // 1. Encontrar la CitaMedica
        CitaMedica cita = citaMedicaRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("La cita médica no existe con ID: " + citaId));

        // 2. Resolver Paciente → Expediente → ConsultaMedica
        Paciente paciente = cita.getPaciente();
        if (paciente == null) {
            throw new IllegalArgumentException("La cita no tiene paciente asociado");
        }

        ExpedienteClinico expediente = expedienteClinicoRepository.findByPacienteId(paciente.getId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el expediente del paciente"));

        // Buscar la ConsultaMedica más reciente de hoy para este expediente
        ConsultaMedica consulta = consultaMedicaRepository
                .findOneWithDetailsById(
                        consultaMedicaRepository
                                .findFirstByExpedienteIdAndFechaConsultaOrderByIdDesc(expediente.getId(), LocalDate.now())
                                .map(ConsultaMedica::getId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "No existe consulta médica registrada para hoy. Finalice la consulta antes de generar el PDF.")))
                .orElseThrow(() -> new IllegalArgumentException("No se pudo cargar la consulta con detalles"));

        return buildPdf(consulta, paciente);
    }

    private byte[] buildPdf(ConsultaMedica consulta, Paciente paciente) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Tipografías
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Título
            Paragraph title = new Paragraph("Ministerio de Salud - Receta Médica", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Información de la Consulta
            document.add(new Paragraph("Consulta No: " + consulta.getId(), headerFont));
            document.add(new Paragraph(
                    "Fecha: " + consulta.getFechaConsulta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    normalFont));

            // Información del Paciente
            document.add(new Paragraph(
                    "Paciente: " + paciente.getNombres() + " " + paciente.getApellidos(), normalFont));
            if (paciente.getCedula() != null) {
                document.add(new Paragraph("Cédula: " + paciente.getCedula(), normalFont));
            }

            document.add(new Paragraph(" ", normalFont));

            // Diagnósticos
            document.add(new Paragraph("Diagnóstico(s):", headerFont));
            if (consulta.getDiagnosticos() != null && !consulta.getDiagnosticos().isEmpty()) {
                for (Diagnostico d : consulta.getDiagnosticos()) {
                    document.add(
                            new Paragraph("  - [" + d.getCodigoCIE() + "] " + d.getDescripcion(), normalFont));
                }
            } else {
                document.add(new Paragraph("  No se registró diagnóstico.", smallFont));
            }
            document.add(new Paragraph(" ", normalFont));

            // Tabla de Recetas
            document.add(new Paragraph("Medicamentos Recetados:", headerFont));
            document.add(new Paragraph(" ", smallFont));

            if (consulta.getRecetas() != null && !consulta.getRecetas().isEmpty()) {
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 3f, 1.5f, 2.5f, 1.5f });

                // Headers de la tabla
                String[] headers = { "Medicamento", "Dosis", "Frecuencia", "Duración" };
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setPadding(8f);
                    table.addCell(cell);
                }

                for (Receta r : consulta.getRecetas()) {
                    String nombreMed = r.getMedicamento() != null ? r.getMedicamento().getNombre() : "N/A";
                    table.addCell(new Phrase(nombreMed, normalFont));
                    table.addCell(new Phrase(r.getDosis(), normalFont));
                    table.addCell(new Phrase(r.getFrecuencia(), normalFont));
                    table.addCell(new Phrase(r.getDuracion(), normalFont));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("  No se recetaron medicamentos.", smallFont));
            }

            // Notas Médicas
            if (consulta.getNotasMedicas() != null && !consulta.getNotasMedicas().isBlank()) {
                document.add(new Paragraph(" ", normalFont));
                document.add(new Paragraph("Notas Médicas:", headerFont));
                document.add(new Paragraph(consulta.getNotasMedicas(), normalFont));
            }

            // Firma (Simulada)
            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph(" ", normalFont));
            Paragraph firma = new Paragraph("___________________________", normalFont);
            firma.setAlignment(Element.ALIGN_CENTER);
            document.add(firma);

            String doctorName = consulta.getUser() != null ? consulta.getUser().getLogin() : "Medico Tratante";
            Paragraph firmaDr = new Paragraph("Dr/Dra. " + doctorName, normalFont);
            firmaDr.setAlignment(Element.ALIGN_CENTER);
            document.add(firmaDr);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | java.io.IOException e) {
            LOG.error("Error al generar el PDF de la receta", e);
            throw new RuntimeException("Error al generar el PDF de la receta", e);
        }
    }
}
