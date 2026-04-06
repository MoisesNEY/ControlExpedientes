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
import java.util.List;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Diagnostico;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.Receta;
import ni.edu.mney.domain.ResultadoLaboratorio;
import ni.edu.mney.domain.SignosVitales;
import ni.edu.mney.domain.Tratamiento;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.repository.PacienteRepository;
import ni.edu.mney.repository.ResultadoLaboratorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de generar el PDF del historial clínico completo de un paciente.
 */
@Service
@Transactional(readOnly = true)
public class ReporteHistorialService {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteHistorialService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PacienteRepository pacienteRepository;
    private final ExpedienteClinicoRepository expedienteClinicoRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final ResultadoLaboratorioRepository resultadoLaboratorioRepository;

    public ReporteHistorialService(
        PacienteRepository pacienteRepository,
        ExpedienteClinicoRepository expedienteClinicoRepository,
        ConsultaMedicaRepository consultaMedicaRepository,
        ResultadoLaboratorioRepository resultadoLaboratorioRepository
    ) {
        this.pacienteRepository = pacienteRepository;
        this.expedienteClinicoRepository = expedienteClinicoRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.resultadoLaboratorioRepository = resultadoLaboratorioRepository;
    }

    /**
     * Genera un PDF con el historial clínico completo del paciente.
     *
     * @param pacienteId el ID del paciente
     * @return el archivo PDF en bytes
     */
    public byte[] generarHistorialPdf(Long pacienteId) {
        LOG.debug("Request para generar historial clínico PDF para Paciente ID: {}", pacienteId);

        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente con ID: " + pacienteId));

        ExpedienteClinico expediente = expedienteClinicoRepository.findByPacienteId(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el expediente del paciente con ID: " + pacienteId));

        List<ConsultaMedica> consultaIds = consultaMedicaRepository
            .findByExpedienteIdOrderByFechaConsultaDesc(expediente.getId());

        List<ConsultaMedica> consultas = consultaIds.stream()
            .map(c -> consultaMedicaRepository.findOneWithDetailsById(c.getId()).orElse(c))
            .toList();

        List<ResultadoLaboratorio> resultados = resultadoLaboratorioRepository
            .findByPacienteId(pacienteId, PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "fechaExamen")))
            .getContent();

        return buildPdf(paciente, expediente, consultas, resultados);
    }

    private byte[] buildPdf(
        Paciente paciente,
        ExpedienteClinico expediente,
        List<ConsultaMedica> consultas,
        List<ResultadoLaboratorio> resultados
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
            Paragraph title = new Paragraph("Ministerio de Salud - Historial Clínico Completo", titleFont);
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
            document.add(new Paragraph(" ", smallFont));
            document.add(new Paragraph("Número de Expediente: " + expediente.getNumeroExpediente(), normalFont));
            document.add(new Paragraph(
                "Fecha de Apertura: " + (expediente.getFechaApertura() != null ? expediente.getFechaApertura().format(DATE_FMT) : "N/A"),
                normalFont
            ));

            // --- Consultations ---
            for (ConsultaMedica consulta : consultas) {
                document.add(new Paragraph(" ", normalFont));
                addSeparatorLine(document);

                Paragraph consultaTitle = new Paragraph(
                    "Consulta del " + consulta.getFechaConsulta().format(DATE_FMT),
                    subheaderFont
                );
                consultaTitle.setSpacingAfter(8f);
                document.add(consultaTitle);

                document.add(new Paragraph("Motivo de Consulta: " + consulta.getMotivoConsulta(), normalFont));

                if (consulta.getNotasMedicas() != null && !consulta.getNotasMedicas().isBlank()) {
                    document.add(new Paragraph("Notas Médicas: " + consulta.getNotasMedicas(), normalFont));
                }

                String doctorName = consulta.getUser() != null ? consulta.getUser().getLogin() : "N/A";
                document.add(new Paragraph("Doctor: " + doctorName, normalFont));
                document.add(new Paragraph(" ", smallFont));

                // Signos Vitales
                if (consulta.getSignosVitales() != null && !consulta.getSignosVitales().isEmpty()) {
                    document.add(new Paragraph("Signos Vitales:", headerFont));
                    document.add(new Paragraph(" ", smallFont));

                    PdfPTable svTable = new PdfPTable(5);
                    svTable.setWidthPercentage(100);
                    svTable.setWidths(new float[] { 1f, 1f, 1.5f, 1f, 1f });

                    String[] svHeaders = { "Peso (kg)", "Altura (m)", "Presión Arterial", "Temp. (°C)", "FC (lpm)" };
                    for (String h : svHeaders) {
                        PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                        cell.setPadding(5f);
                        svTable.addCell(cell);
                    }

                    for (SignosVitales sv : consulta.getSignosVitales()) {
                        svTable.addCell(new Phrase(sv.getPeso() != null ? sv.getPeso().toString() : "-", normalFont));
                        svTable.addCell(new Phrase(sv.getAltura() != null ? sv.getAltura().toString() : "-", normalFont));
                        svTable.addCell(new Phrase(sv.getPresionArterial() != null ? sv.getPresionArterial() : "-", normalFont));
                        svTable.addCell(new Phrase(sv.getTemperatura() != null ? sv.getTemperatura().toString() : "-", normalFont));
                        svTable.addCell(new Phrase(
                            sv.getFrecuenciaCardiaca() != null ? sv.getFrecuenciaCardiaca().toString() : "-", normalFont
                        ));
                    }
                    document.add(svTable);
                    document.add(new Paragraph(" ", smallFont));
                }

                // Diagnósticos
                if (consulta.getDiagnosticos() != null && !consulta.getDiagnosticos().isEmpty()) {
                    document.add(new Paragraph("Diagnósticos:", headerFont));
                    for (Diagnostico d : consulta.getDiagnosticos()) {
                        String cie = d.getCodigoCIE() != null ? "[" + d.getCodigoCIE() + "] " : "";
                        document.add(new Paragraph("  - " + cie + d.getDescripcion(), normalFont));
                    }
                    document.add(new Paragraph(" ", smallFont));
                }

                // Tratamientos
                if (consulta.getTratamientos() != null && !consulta.getTratamientos().isEmpty()) {
                    document.add(new Paragraph("Tratamientos:", headerFont));
                    for (Tratamiento t : consulta.getTratamientos()) {
                        String duracion = t.getDuracionDias() != null ? " (" + t.getDuracionDias() + " días)" : "";
                        document.add(new Paragraph("  - " + t.getIndicaciones() + duracion, normalFont));
                    }
                    document.add(new Paragraph(" ", smallFont));
                }

                // Recetas
                if (consulta.getRecetas() != null && !consulta.getRecetas().isEmpty()) {
                    document.add(new Paragraph("Recetas:", headerFont));
                    document.add(new Paragraph(" ", smallFont));

                    PdfPTable recetaTable = new PdfPTable(4);
                    recetaTable.setWidthPercentage(100);
                    recetaTable.setWidths(new float[] { 3f, 1.5f, 2.5f, 1.5f });

                    String[] recetaHeaders = { "Medicamento", "Dosis", "Frecuencia", "Duración" };
                    for (String h : recetaHeaders) {
                        PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                        cell.setPadding(5f);
                        recetaTable.addCell(cell);
                    }

                    for (Receta r : consulta.getRecetas()) {
                        String nombreMed = r.getMedicamento() != null ? r.getMedicamento().getNombre() : "N/A";
                        recetaTable.addCell(new Phrase(nombreMed, normalFont));
                        recetaTable.addCell(new Phrase(r.getDosis(), normalFont));
                        recetaTable.addCell(new Phrase(r.getFrecuencia(), normalFont));
                        recetaTable.addCell(new Phrase(r.getDuracion(), normalFont));
                    }
                    document.add(recetaTable);
                }
            }

            // --- Lab Results ---
            if (!resultados.isEmpty()) {
                document.add(new Paragraph(" ", normalFont));
                addSeparatorLine(document);

                Paragraph labTitle = new Paragraph("Resultados de Laboratorio", subheaderFont);
                labTitle.setSpacingAfter(8f);
                document.add(labTitle);

                PdfPTable labTable = new PdfPTable(5);
                labTable.setWidthPercentage(100);
                labTable.setWidths(new float[] { 2.5f, 2f, 2f, 1f, 1.5f });

                String[] labHeaders = { "Tipo Examen", "Resultado", "Valor Referencia", "Unidad", "Fecha" };
                for (String h : labHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setPadding(5f);
                    labTable.addCell(cell);
                }

                for (ResultadoLaboratorio rl : resultados) {
                    labTable.addCell(new Phrase(rl.getTipoExamen(), normalFont));
                    labTable.addCell(new Phrase(rl.getResultado(), normalFont));
                    labTable.addCell(new Phrase(rl.getValorReferencia() != null ? rl.getValorReferencia() : "-", normalFont));
                    labTable.addCell(new Phrase(rl.getUnidad() != null ? rl.getUnidad() : "-", normalFont));
                    labTable.addCell(new Phrase(rl.getFechaExamen().format(DATE_FMT), normalFont));
                }
                document.add(labTable);
            }

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | java.io.IOException e) {
            LOG.error("Error al generar el PDF del historial clínico", e);
            throw new RuntimeException("Error al generar el PDF del historial clínico", e);
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
