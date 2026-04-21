package ni.edu.mney.service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
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
import ni.edu.mney.service.report.PdfReportSupport;
import ni.edu.mney.service.report.PdfReportSupport.InfoItem;
import ni.edu.mney.service.report.ReportTextUtils;
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
            com.lowagie.text.Document document = PdfReportSupport.newDocument();
            PdfWriter.getInstance(document, baos);
            document.open();

            PdfReportSupport.Fonts fonts = PdfReportSupport.fonts();
            PdfReportSupport.addHeader(
                document,
                fonts,
                "Historial clínico",
                "Seguimiento completo de atenciones y laboratorios",
                "Expediente",
                expediente.getNumeroExpediente(),
                LocalDate.now()
            );
            PdfReportSupport.addInfoGrid(document, fonts, List.of(
                new InfoItem("Paciente", ReportTextUtils.fullName(paciente.getNombres(), paciente.getApellidos(), "Paciente")),
                new InfoItem("Cédula", ReportTextUtils.defaultText(paciente.getCedula())),
                new InfoItem("Sexo", paciente.getSexo() != null ? paciente.getSexo().toString() : "N/D"),
                new InfoItem("Fecha de nacimiento", paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().format(DATE_FMT) : "N/D"),
                new InfoItem("Estado civil", paciente.getEstadoCivil() != null ? paciente.getEstadoCivil().toString() : "N/D"),
                new InfoItem("Teléfono", ReportTextUtils.defaultText(paciente.getTelefono())),
                new InfoItem("Dirección", ReportTextUtils.defaultText(paciente.getDireccion())),
                new InfoItem("Correo", ReportTextUtils.defaultText(paciente.getEmail())),
                new InfoItem("Fecha de apertura", expediente.getFechaApertura() != null ? expediente.getFechaApertura().format(DATE_FMT) : "N/D"),
                new InfoItem("Total de consultas", String.valueOf(consultas.size()))
            ));

            for (ConsultaMedica consulta : consultas) {
                PdfReportSupport.addSectionTitle(document, fonts, "Consulta del " + consulta.getFechaConsulta().format(DATE_FMT));

                document.add(new Paragraph("Motivo de consulta: " + ReportTextUtils.defaultText(consulta.getMotivoConsulta()), fonts.normal()));

                if (consulta.getNotasMedicas() != null && !consulta.getNotasMedicas().isBlank()) {
                    document.add(new Paragraph("Notas médicas: " + consulta.getNotasMedicas(), fonts.normal()));
                }

                String doctorName = consulta.getUser() != null
                    ? ReportTextUtils.fullName(consulta.getUser().getFirstName(), consulta.getUser().getLastName(), consulta.getUser().getLogin())
                    : "N/A";
                document.add(new Paragraph("Doctor: " + doctorName, fonts.normal()));

                if (consulta.getSignosVitales() != null && !consulta.getSignosVitales().isEmpty()) {
                    PdfReportSupport.addSectionTitle(document, fonts, "Signos vitales");

                    PdfPTable svTable = PdfReportSupport.createTable(
                        fonts,
                        new float[] { 1f, 1f, 1.5f, 1f, 1f },
                        "Peso (kg)",
                        "Altura (m)",
                        "Presión arterial",
                        "Temp. (°C)",
                        "FC (lpm)"
                    );

                    for (SignosVitales sv : consulta.getSignosVitales()) {
                        svTable.addCell(PdfReportSupport.createBodyCell(sv.getPeso() != null ? sv.getPeso().toString() : "-", fonts.small()));
                        svTable.addCell(PdfReportSupport.createBodyCell(sv.getAltura() != null ? sv.getAltura().toString() : "-", fonts.small()));
                        svTable.addCell(PdfReportSupport.createBodyCell(sv.getPresionArterial() != null ? sv.getPresionArterial() : "-", fonts.small()));
                        svTable.addCell(PdfReportSupport.createBodyCell(sv.getTemperatura() != null ? sv.getTemperatura().toString() : "-", fonts.small()));
                        svTable.addCell(PdfReportSupport.createBodyCell(
                            sv.getFrecuenciaCardiaca() != null ? sv.getFrecuenciaCardiaca().toString() : "-", fonts.small()
                        ));
                    }
                    document.add(svTable);
                }

                if (consulta.getDiagnosticos() != null && !consulta.getDiagnosticos().isEmpty()) {
                    PdfReportSupport.addSectionTitle(document, fonts, "Diagnósticos");
                    for (Diagnostico d : consulta.getDiagnosticos()) {
                        String cie = d.getCodigoCIE() != null ? "[" + d.getCodigoCIE() + "] " : "";
                        document.add(new Paragraph("• " + cie + d.getDescripcion(), fonts.normal()));
                    }
                }

                if (consulta.getTratamientos() != null && !consulta.getTratamientos().isEmpty()) {
                    PdfReportSupport.addSectionTitle(document, fonts, "Tratamientos");
                    for (Tratamiento t : consulta.getTratamientos()) {
                        String duracion = t.getDuracionDias() != null ? " (" + t.getDuracionDias() + " días)" : "";
                        document.add(new Paragraph("• " + t.getIndicaciones() + duracion, fonts.normal()));
                    }
                }

                if (consulta.getRecetas() != null && !consulta.getRecetas().isEmpty()) {
                    PdfReportSupport.addSectionTitle(document, fonts, "Recetas");

                    PdfPTable recetaTable = PdfReportSupport.createTable(
                        fonts,
                        new float[] { 3f, 1.5f, 2.5f, 1.5f },
                        "Medicamento",
                        "Dosis",
                        "Frecuencia",
                        "Duración"
                    );

                    for (Receta r : consulta.getRecetas()) {
                        String nombreMed = r.getMedicamento() != null ? r.getMedicamento().getNombre() : "N/A";
                        recetaTable.addCell(PdfReportSupport.createBodyCell(nombreMed, fonts.normal()));
                        recetaTable.addCell(PdfReportSupport.createBodyCell(r.getDosis(), fonts.small()));
                        recetaTable.addCell(PdfReportSupport.createBodyCell(r.getFrecuencia(), fonts.small()));
                        recetaTable.addCell(PdfReportSupport.createBodyCell(r.getDuracion(), fonts.small()));
                    }
                    document.add(recetaTable);
                }
            }

            if (!resultados.isEmpty()) {
                PdfReportSupport.addSectionTitle(document, fonts, "Resultados de laboratorio");

                PdfPTable labTable = PdfReportSupport.createTable(
                    fonts,
                    new float[] { 2.5f, 2f, 2f, 1f, 1.5f },
                    "Tipo examen",
                    "Resultado",
                    "Valor referencia",
                    "Unidad",
                    "Fecha"
                );

                for (ResultadoLaboratorio rl : resultados) {
                    labTable.addCell(PdfReportSupport.createBodyCell(rl.getTipoExamen(), fonts.normal()));
                    labTable.addCell(PdfReportSupport.createBodyCell(rl.getResultado(), fonts.small()));
                    labTable.addCell(PdfReportSupport.createBodyCell(rl.getValorReferencia() != null ? rl.getValorReferencia() : "-", fonts.small()));
                    labTable.addCell(PdfReportSupport.createBodyCell(rl.getUnidad() != null ? rl.getUnidad() : "-", fonts.small()));
                    labTable.addCell(PdfReportSupport.createBodyCell(rl.getFechaExamen().format(DATE_FMT), fonts.small()));
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

}
