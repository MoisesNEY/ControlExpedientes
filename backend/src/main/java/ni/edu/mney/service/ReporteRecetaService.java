package ni.edu.mney.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Diagnostico;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.Receta;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.service.dto.ReporteRecetaPreviewRequestDTO;
import ni.edu.mney.service.report.PdfReportSupport;
import ni.edu.mney.service.report.PdfReportSupport.InfoItem;
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

    public byte[] generarRecetaPdf(Long citaId) {
        LOG.debug("Request para generar receta PDF para CitaMedica ID: {}", citaId);

        CitaMedica cita = citaMedicaRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("La cita médica no existe con ID: " + citaId));

        Paciente paciente = cita.getPaciente();
        if (paciente == null) {
            throw new IllegalArgumentException("La cita no tiene paciente asociado");
        }

        ExpedienteClinico expediente = expedienteClinicoRepository.findByPacienteId(paciente.getId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el expediente del paciente"));

        ConsultaMedica consulta = consultaMedicaRepository.findByExpedienteIdOrderByFechaConsultaDesc(expediente.getId())
                .stream()
                .findFirst()
                .flatMap(item -> consultaMedicaRepository.findOneWithDetailsById(item.getId()))
                .orElseThrow(() -> new IllegalArgumentException("No existe una consulta médica registrada para este paciente."));

        return buildPdf(toData(consulta, paciente, cita.getId()));
    }

    public byte[] generarRecetaPdfPorConsulta(Long consultaId) {
        LOG.debug("Request para generar receta PDF para ConsultaMedica ID: {}", consultaId);

        ConsultaMedica consulta = consultaMedicaRepository.findOneWithDetailsById(consultaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la consulta médica con ID: " + consultaId));
        Paciente paciente = consulta.getExpediente() != null ? consulta.getExpediente().getPaciente() : null;
        if (paciente == null) {
            throw new IllegalArgumentException("La consulta seleccionada no tiene un paciente asociado.");
        }
        return buildPdf(toData(consulta, paciente, null));
    }

    public byte[] generarRecetaPreviewPdf(ReporteRecetaPreviewRequestDTO request) {
        LOG.debug("Request para generar receta PDF preliminar para cita ID: {}", request.getCitaId());

        if (request.getNombrePaciente() == null || request.getNombrePaciente().isBlank()) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio para generar la receta.");
        }
        if (request.getDescripcionDiagnostico() == null || request.getDescripcionDiagnostico().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un diagnóstico para generar la receta.");
        }

        return buildPdf(new RecetaReportData(
                request.getCitaId(),
                request.getFechaConsulta() != null ? request.getFechaConsulta() : LocalDate.now(),
                request.getNombrePaciente(),
                request.getCodigoPaciente(),
                request.getCedulaPaciente(),
                request.getMotivoConsulta(),
                request.getCodigoDiagnostico(),
                request.getDescripcionDiagnostico(),
                request.getNotasMedicas(),
                request.getDoctorName(),
                request.getRecetas() == null ? List.of() : request.getRecetas().stream()
                        .map(item -> new RecetaItem(
                                item.getMedicamento(),
                                item.getDosis(),
                                item.getFrecuencia(),
                                item.getDuracion()))
                        .toList()));
    }

    private RecetaReportData toData(ConsultaMedica consulta, Paciente paciente, Long citaId) {
        Diagnostico principal = consulta.getDiagnosticos() == null
                ? null
                : consulta.getDiagnosticos().stream().findFirst().orElse(null);

        return new RecetaReportData(
                citaId,
                consulta.getFechaConsulta(),
                (paciente.getNombres() + " " + paciente.getApellidos()).trim(),
                paciente.getCodigo(),
                paciente.getCedula(),
                consulta.getMotivoConsulta(),
                principal != null ? principal.getCodigoCIE() : null,
                principal != null ? principal.getDescripcion() : null,
                consulta.getNotasMedicas(),
                consulta.getUser() != null
                        ? ((consulta.getUser().getFirstName() + " " + consulta.getUser().getLastName()).trim().isBlank()
                                ? consulta.getUser().getLogin()
                                : (consulta.getUser().getFirstName() + " " + consulta.getUser().getLastName()).trim())
                        : "Médico Tratante",
                consulta.getRecetas() == null ? List.of() : consulta.getRecetas().stream()
                        .map(receta -> new RecetaItem(
                                receta.getMedicamento() != null ? receta.getMedicamento().getNombre() : "Medicamento",
                                receta.getDosis(),
                                receta.getFrecuencia(),
                                receta.getDuracion()))
                        .toList());
    }

    private byte[] buildPdf(RecetaReportData data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = PdfReportSupport.newDocument();
            PdfWriter.getInstance(document, baos);
            document.open();

            PdfReportSupport.Fonts fonts = PdfReportSupport.fonts();
            PdfReportSupport.addHeader(
                    document,
                    fonts,
                    "Receta médica",
                    "Documento clínico generado por el sistema",
                    data.citaId() != null ? "Cita" : "Consulta",
                    data.citaId() != null ? String.valueOf(data.citaId()) : "Resumen clínico",
                    data.fechaConsulta());
            PdfReportSupport.addInfoGrid(document, fonts, List.of(
                    new InfoItem("Paciente", defaultText(data.nombrePaciente())),
                    new InfoItem("Código paciente", defaultText(data.codigoPaciente())),
                    new InfoItem("Cédula", defaultText(data.cedulaPaciente())),
                    new InfoItem("Médico tratante", defaultText(data.doctorName())),
                    new InfoItem("Fecha de atención", PdfReportSupport.formatDate(data.fechaConsulta())),
                    new InfoItem("Motivo de consulta", defaultText(data.motivoConsulta()))));

            PdfReportSupport.addSectionTitle(document, fonts, "Diagnóstico principal");
            if (data.descripcionDiagnostico() != null && !data.descripcionDiagnostico().isBlank()) {
                PdfReportSupport.addInfoGrid(document, fonts, List.of(
                        new InfoItem("Código CIE-10", defaultText(data.codigoDiagnostico())),
                        new InfoItem("Descripción", defaultText(data.descripcionDiagnostico()))));
            } else {
                PdfReportSupport.addEmptyState(document, fonts, "No se registró diagnóstico principal.");
            }

            PdfReportSupport.addSectionTitle(document, fonts, "Tratamiento farmacológico");
            if (!data.recetas().isEmpty()) {
                PdfPTable table = PdfReportSupport.createTable(
                        fonts,
                        new float[] { 3f, 1.5f, 2.5f, 1.5f },
                        "Medicamento",
                        "Dosis",
                        "Frecuencia",
                        "Duración");
                for (RecetaItem receta : data.recetas()) {
                    table.addCell(PdfReportSupport.createBodyCell(defaultText(receta.medicamento()), fonts.normal()));
                    table.addCell(PdfReportSupport.createBodyCell(defaultText(receta.dosis()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(defaultText(receta.frecuencia()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(defaultText(receta.duracion()), fonts.small()));
                }
                document.add(table);
            } else {
                PdfReportSupport.addEmptyState(document, fonts, "No se prescribieron medicamentos en esta receta.");
            }

            if (data.notasMedicas() != null && !data.notasMedicas().isBlank()) {
                PdfReportSupport.addSectionTitle(document, fonts, "Notas clínicas");
                PdfReportSupport.addEmptyState(document, fonts, data.notasMedicas());
            }

            PdfReportSupport.addSignature(document, fonts, defaultText(data.doctorName()));

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | java.io.IOException e) {
            LOG.error("Error al generar el PDF de la receta", e);
            throw new RuntimeException("Error al generar el PDF de la receta", e);
        }
    }

    private String defaultText(String value) {
        return value != null && !value.isBlank() ? value : "N/D";
    }

    private record RecetaReportData(
            Long citaId,
            LocalDate fechaConsulta,
            String nombrePaciente,
            String codigoPaciente,
            String cedulaPaciente,
            String motivoConsulta,
            String codigoDiagnostico,
            String descripcionDiagnostico,
            String notasMedicas,
            String doctorName,
            List<RecetaItem> recetas) {}

    private record RecetaItem(String medicamento, String dosis, String frecuencia, String duracion) {}
}
