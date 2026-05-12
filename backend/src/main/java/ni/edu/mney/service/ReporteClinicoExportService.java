package ni.edu.mney.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.ResultadoLaboratorio;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.repository.PacienteRepository;
import ni.edu.mney.repository.ResultadoLaboratorioRepository;
import ni.edu.mney.service.report.PdfReportSupport;
import ni.edu.mney.service.report.PdfReportSupport.InfoItem;
import ni.edu.mney.service.report.ReportTextUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReporteClinicoExportService {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteClinicoExportService.class);
    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final PacienteRepository pacienteRepository;
    private final ExpedienteClinicoRepository expedienteClinicoRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final ResultadoLaboratorioRepository resultadoLaboratorioRepository;
    private final SystemSettingsService systemSettingsService;

    public ReporteClinicoExportService(
        PacienteRepository pacienteRepository,
        ExpedienteClinicoRepository expedienteClinicoRepository,
        ConsultaMedicaRepository consultaMedicaRepository,
        ResultadoLaboratorioRepository resultadoLaboratorioRepository,
        SystemSettingsService systemSettingsService
    ) {
        this.pacienteRepository = pacienteRepository;
        this.expedienteClinicoRepository = expedienteClinicoRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.resultadoLaboratorioRepository = resultadoLaboratorioRepository;
        this.systemSettingsService = systemSettingsService;
    }

    public AdminSecurityExportService.ExportedSpreadsheet generarHistorialExcel(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente con ID: " + pacienteId));
        ExpedienteClinico expediente = expedienteClinicoRepository.findByPacienteId(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el expediente del paciente con ID: " + pacienteId));
        List<ConsultaMedica> consultas = consultaMedicaRepository.findByExpedienteIdOrderByFechaConsultaDesc(expediente.getId()).stream()
            .map(consulta -> consultaMedicaRepository.findOneWithDetailsById(consulta.getId()).orElse(consulta))
            .toList();
        List<ResultadoLaboratorio> resultados = resultadoLaboratorioRepository
            .findByPacienteId(pacienteId, PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "fechaExamen")))
            .getContent();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addCoverSheet(workbook, "Historial clínico", "Consolidado de consultas, recetas y laboratorios", List.of(
                new InfoItem("Paciente", fullName(paciente)),
                new InfoItem("Expediente", expediente.getNumeroExpediente()),
                new InfoItem("Consultas", String.valueOf(consultas.size())),
                new InfoItem("Laboratorios", String.valueOf(resultados.size()))
            ));

            Sheet resumenSheet = workbook.createSheet("Resumen");
            writeHeader(resumenSheet.createRow(0), "Campo", "Valor");
            writeCell(resumenSheet.createRow(1), 0, "Paciente");
            writeCell(resumenSheet.getRow(1), 1, fullName(paciente));
            writeCell(resumenSheet.createRow(2), 0, "Expediente");
            writeCell(resumenSheet.getRow(2), 1, expediente.getNumeroExpediente());
            writeCell(resumenSheet.createRow(3), 0, "Total consultas");
            writeCell(resumenSheet.getRow(3), 1, String.valueOf(consultas.size()));
            writeCell(resumenSheet.createRow(4), 0, "Total laboratorios");
            writeCell(resumenSheet.getRow(4), 1, String.valueOf(resultados.size()));
            autoSize(resumenSheet, 2);

            Sheet consultasSheet = workbook.createSheet("Consultas");
            writeHeader(consultasSheet.createRow(0), "Fecha", "Motivo", "Profesional", "Diagnósticos", "Recetas", "Notas");
            int consultaRowIndex = 1;
            for (ConsultaMedica consulta : consultas) {
                Row row = consultasSheet.createRow(consultaRowIndex++);
                writeCell(row, 0, formatDate(consulta.getFechaConsulta()));
                writeCell(row, 1, consulta.getMotivoConsulta());
                writeCell(row, 2, doctorName(consulta));
                writeCell(row, 3, diagnosticosTexto(consulta));
                writeCell(row, 4, recetasTexto(consulta));
                writeCell(row, 5, consulta.getNotasMedicas());
            }
            autoSize(consultasSheet, 6);

            Sheet labSheet = workbook.createSheet("Laboratorio");
            writeHeader(labSheet.createRow(0), "Fecha", "Tipo examen", "Resultado", "Referencia", "Unidad", "Observaciones");
            int labRowIndex = 1;
            for (ResultadoLaboratorio resultado : resultados) {
                Row row = labSheet.createRow(labRowIndex++);
                writeCell(row, 0, formatDate(resultado.getFechaExamen()));
                writeCell(row, 1, resultado.getTipoExamen());
                writeCell(row, 2, resultado.getResultado());
                writeCell(row, 3, resultado.getValorReferencia());
                writeCell(row, 4, resultado.getUnidad());
                writeCell(row, 5, resultado.getObservaciones());
            }
            autoSize(labSheet, 6);

            workbook.write(output);
            return new AdminSecurityExportService.ExportedSpreadsheet(
                "historial-clinico-" + pacienteId + "-" + FILE_DATE_FORMAT.format(LocalDate.now()) + ".xlsx",
                CONTENT_TYPE,
                output.toByteArray()
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel del historial clínico.", e);
        }
    }

    public AdminSecurityExportService.ExportedSpreadsheet generarExpedienteExcel(Long expedienteId) {
        ExpedienteClinico expediente = expedienteClinicoRepository.findById(expedienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el expediente clínico con ID: " + expedienteId));
        Paciente paciente = pacienteRepository.findByExpedienteId(expedienteId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente del expediente con ID: " + expedienteId));
        List<ConsultaMedica> consultas = consultaMedicaRepository.findByExpedienteIdOrderByFechaConsultaDesc(expedienteId).stream()
            .map(consulta -> consultaMedicaRepository.findOneWithDetailsById(consulta.getId()).orElse(consulta))
            .toList();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addCoverSheet(workbook, "Expediente clínico", "Resumen administrativo y clínico del expediente", List.of(
                new InfoItem("Paciente", fullName(paciente)),
                new InfoItem("Expediente", expediente.getNumeroExpediente()),
                new InfoItem("Fecha de apertura", formatDate(expediente.getFechaApertura())),
                new InfoItem("Consultas", String.valueOf(consultas.size()))
            ));

            Sheet resumenSheet = workbook.createSheet("Expediente");
            writeHeader(resumenSheet.createRow(0), "Campo", "Valor");
            writeCell(resumenSheet.createRow(1), 0, "Número expediente");
            writeCell(resumenSheet.getRow(1), 1, expediente.getNumeroExpediente());
            writeCell(resumenSheet.createRow(2), 0, "Paciente");
            writeCell(resumenSheet.getRow(2), 1, fullName(paciente));
            writeCell(resumenSheet.createRow(3), 0, "Fecha apertura");
            writeCell(resumenSheet.getRow(3), 1, formatDate(expediente.getFechaApertura()));
            writeCell(resumenSheet.createRow(4), 0, "Observaciones");
            writeCell(resumenSheet.getRow(4), 1, expediente.getObservaciones());
            writeCell(resumenSheet.createRow(5), 0, "Total consultas");
            writeCell(resumenSheet.getRow(5), 1, String.valueOf(consultas.size()));
            autoSize(resumenSheet, 2);

            Sheet consultasSheet = workbook.createSheet("Últimas consultas");
            writeHeader(consultasSheet.createRow(0), "Fecha", "Motivo", "Profesional", "Diagnósticos", "Recetas");
            int rowIndex = 1;
            for (ConsultaMedica consulta : consultas.stream().limit(20).toList()) {
                Row row = consultasSheet.createRow(rowIndex++);
                writeCell(row, 0, formatDate(consulta.getFechaConsulta()));
                writeCell(row, 1, consulta.getMotivoConsulta());
                writeCell(row, 2, doctorName(consulta));
                writeCell(row, 3, diagnosticosTexto(consulta));
                writeCell(row, 4, recetasTexto(consulta));
            }
            autoSize(consultasSheet, 5);

            workbook.write(output);
            return new AdminSecurityExportService.ExportedSpreadsheet(
                "expediente-" + expedienteId + "-" + FILE_DATE_FORMAT.format(LocalDate.now()) + ".xlsx",
                CONTENT_TYPE,
                output.toByteArray()
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel del expediente clínico.", e);
        }
    }

    public byte[] generarResumenConsultasPdf(LocalDate fechaInicio, LocalDate fechaFin, Long pacienteId, String doctorLogin) {
        List<ConsultaMedica> consultas = resolveConsultas(fechaInicio, fechaFin, pacienteId, doctorLogin);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = PdfReportSupport.newDocument();
            PdfWriter.getInstance(document, baos);
            document.open();

            PdfReportSupport.Fonts fonts = PdfReportSupport.fonts();
            var settings = systemSettingsService.getSettings();
            PdfReportSupport.addHeader(
                document,
                fonts,
                settings.brandName(),
                settings.applicationName(),
                "Resumen de consultas",
                "Reporte filtrado por rango de fechas",
                "Periodo",
                formatDate(fechaInicio) + " - " + formatDate(fechaFin),
                LocalDate.now()
            );
            PdfReportSupport.addInfoGrid(document, fonts, List.of(
                new InfoItem("Paciente", pacienteId != null ? resolvePatientLabel(pacienteId) : "Todos"),
                new InfoItem("Médico", resolveDoctorLabel(doctorLogin, consultas)),
                new InfoItem("Consultas encontradas", String.valueOf(consultas.size()))
            ));

            if (consultas.isEmpty()) {
                PdfReportSupport.addEmptyState(document, fonts, "No se encontraron consultas para los filtros seleccionados.");
            } else {
                PdfReportSupport.addSectionTitle(document, fonts, "Detalle de consultas");
                PdfPTable table = PdfReportSupport.createTable(
                    fonts,
                    new float[] { 1.4f, 2.6f, 2.3f, 2.7f, 2.3f },
                    "Fecha",
                    "Paciente",
                    "Profesional",
                    "Motivo",
                    "Diagnóstico principal"
                );
                for (ConsultaMedica consulta : consultas) {
                    table.addCell(PdfReportSupport.createBodyCell(formatDate(consulta.getFechaConsulta()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(patientName(consulta), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(doctorName(consulta), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(ReportTextUtils.defaultText(consulta.getMotivoConsulta()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(diagnosticoPrincipal(consulta), fonts.small()));
                }
                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            LOG.error("No se pudo generar el PDF de resumen de consultas", e);
            throw new IllegalStateException("No se pudo generar el PDF de resumen de consultas.", e);
        }
    }

    public AdminSecurityExportService.ExportedSpreadsheet generarResumenConsultasExcel(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long pacienteId,
        String doctorLogin
    ) {
        List<ConsultaMedica> consultas = resolveConsultas(fechaInicio, fechaFin, pacienteId, doctorLogin);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addCoverSheet(workbook, "Resumen de consultas", "Reporte filtrado por rango de fechas", List.of(
                new InfoItem("Periodo", formatDate(fechaInicio) + " - " + formatDate(fechaFin)),
                new InfoItem("Paciente", pacienteId != null ? resolvePatientLabel(pacienteId) : "Todos"),
                new InfoItem("Médico", resolveDoctorLabel(doctorLogin, consultas)),
                new InfoItem("Consultas", String.valueOf(consultas.size()))
            ));

            Sheet sheet = workbook.createSheet("Resumen consultas");
            writeHeader(sheet.createRow(0), "Fecha", "Paciente", "Profesional", "Motivo", "Diagnósticos", "Recetas", "Notas");
            int rowIndex = 1;
            for (ConsultaMedica consulta : consultas) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, formatDate(consulta.getFechaConsulta()));
                writeCell(row, 1, patientName(consulta));
                writeCell(row, 2, doctorName(consulta));
                writeCell(row, 3, consulta.getMotivoConsulta());
                writeCell(row, 4, diagnosticosTexto(consulta));
                writeCell(row, 5, recetasTexto(consulta));
                writeCell(row, 6, consulta.getNotasMedicas());
            }
            autoSize(sheet, 7);
            workbook.write(output);
            return new AdminSecurityExportService.ExportedSpreadsheet(
                "consultas-" + FILE_DATE_FORMAT.format(fechaInicio) + "-" + FILE_DATE_FORMAT.format(fechaFin) + ".xlsx",
                CONTENT_TYPE,
                output.toByteArray()
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel de resumen de consultas.", e);
        }
    }

    public byte[] generarMovimientosSistemaPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        validateDateRange(fechaInicio, fechaFin);
        List<ConsultaMedica> consultas = resolveConsultas(fechaInicio, fechaFin, null, null);
        List<ResultadoLaboratorio> resultados = resolveResultadosLaboratorio(fechaInicio, fechaFin);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = PdfReportSupport.newDocument();
            PdfWriter.getInstance(document, baos);
            document.open();

            PdfReportSupport.Fonts fonts = PdfReportSupport.fonts();
            var settings = systemSettingsService.getSettings();
            PdfReportSupport.addHeader(
                document,
                fonts,
                settings.brandName(),
                settings.applicationName(),
                "Movimientos del sistema",
                "Actividad clínica consolidada de todo el sistema",
                "Periodo",
                formatDate(fechaInicio) + " - " + formatDate(fechaFin),
                LocalDate.now()
            );
            PdfReportSupport.addInfoGrid(document, fonts, List.of(
                new InfoItem("Consultas", String.valueOf(consultas.size())),
                new InfoItem("Laboratorios", String.valueOf(resultados.size())),
                new InfoItem("Pacientes atendidos", String.valueOf(countDistinctPatients(consultas, resultados))),
                new InfoItem("Profesionales", String.valueOf(countDistinctDoctors(consultas)))
            ));

            PdfReportSupport.addSectionTitle(document, fonts, "Consultas recientes");
            if (consultas.isEmpty()) {
                PdfReportSupport.addEmptyState(document, fonts, "No se encontraron consultas en el periodo seleccionado.");
            } else {
                PdfPTable table = PdfReportSupport.createTable(
                    fonts,
                    new float[] { 1.1f, 2.2f, 2.1f, 2.4f, 2.4f },
                    "Fecha",
                    "Paciente",
                    "Profesional",
                    "Motivo",
                    "Diagnóstico"
                );
                for (ConsultaMedica consulta : consultas.stream().limit(40).toList()) {
                    table.addCell(PdfReportSupport.createBodyCell(formatDate(consulta.getFechaConsulta()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(patientName(consulta), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(doctorName(consulta), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(ReportTextUtils.defaultText(consulta.getMotivoConsulta()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(diagnosticoPrincipal(consulta), fonts.small()));
                }
                document.add(table);
            }

            PdfReportSupport.addSectionTitle(document, fonts, "Laboratorios recientes");
            if (resultados.isEmpty()) {
                PdfReportSupport.addEmptyState(document, fonts, "No se encontraron resultados de laboratorio en el periodo seleccionado.");
            } else {
                PdfPTable table = PdfReportSupport.createTable(
                    fonts,
                    new float[] { 1.1f, 2.2f, 2.1f, 2.2f, 2.4f },
                    "Fecha",
                    "Paciente",
                    "Examen",
                    "Resultado",
                    "Observaciones"
                );
                for (ResultadoLaboratorio resultado : resultados.stream().limit(40).toList()) {
                    table.addCell(PdfReportSupport.createBodyCell(formatDate(resultado.getFechaExamen()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(patientName(resultado), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(ReportTextUtils.defaultText(resultado.getTipoExamen()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(ReportTextUtils.defaultText(resultado.getResultado()), fonts.small()));
                    table.addCell(PdfReportSupport.createBodyCell(ReportTextUtils.defaultText(resultado.getObservaciones()), fonts.small()));
                }
                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            LOG.error("No se pudo generar el PDF de movimientos del sistema", e);
            throw new IllegalStateException("No se pudo generar el PDF de movimientos del sistema.", e);
        }
    }

    public AdminSecurityExportService.ExportedSpreadsheet generarMovimientosSistemaExcel(LocalDate fechaInicio, LocalDate fechaFin) {
        validateDateRange(fechaInicio, fechaFin);
        List<ConsultaMedica> consultas = resolveConsultas(fechaInicio, fechaFin, null, null);
        List<ResultadoLaboratorio> resultados = resolveResultadosLaboratorio(fechaInicio, fechaFin);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addCoverSheet(workbook, "Movimientos del sistema", "Actividad clínica consolidada de todo el sistema", List.of(
                new InfoItem("Periodo", formatDate(fechaInicio) + " - " + formatDate(fechaFin)),
                new InfoItem("Consultas", String.valueOf(consultas.size())),
                new InfoItem("Laboratorios", String.valueOf(resultados.size())),
                new InfoItem("Pacientes atendidos", String.valueOf(countDistinctPatients(consultas, resultados))),
                new InfoItem("Profesionales", String.valueOf(countDistinctDoctors(consultas)))
            ));

            Sheet consultasSheet = workbook.createSheet("Consultas");
            writeHeader(consultasSheet.createRow(0), "Fecha", "Paciente", "Profesional", "Motivo", "Diagnósticos", "Recetas", "Notas");
            int consultaRowIndex = 1;
            for (ConsultaMedica consulta : consultas) {
                Row row = consultasSheet.createRow(consultaRowIndex++);
                writeCell(row, 0, formatDate(consulta.getFechaConsulta()));
                writeCell(row, 1, patientName(consulta));
                writeCell(row, 2, doctorName(consulta));
                writeCell(row, 3, consulta.getMotivoConsulta());
                writeCell(row, 4, diagnosticosTexto(consulta));
                writeCell(row, 5, recetasTexto(consulta));
                writeCell(row, 6, consulta.getNotasMedicas());
            }
            autoSize(consultasSheet, 7);

            Sheet laboratorioSheet = workbook.createSheet("Laboratorios");
            writeHeader(laboratorioSheet.createRow(0), "Fecha", "Paciente", "Profesional", "Examen", "Resultado", "Referencia", "Unidad", "Observaciones");
            int labRowIndex = 1;
            for (ResultadoLaboratorio resultado : resultados) {
                Row row = laboratorioSheet.createRow(labRowIndex++);
                writeCell(row, 0, formatDate(resultado.getFechaExamen()));
                writeCell(row, 1, patientName(resultado));
                writeCell(row, 2, resultado.getConsulta() != null ? doctorName(resultado.getConsulta()) : "N/D");
                writeCell(row, 3, resultado.getTipoExamen());
                writeCell(row, 4, resultado.getResultado());
                writeCell(row, 5, resultado.getValorReferencia());
                writeCell(row, 6, resultado.getUnidad());
                writeCell(row, 7, resultado.getObservaciones());
            }
            autoSize(laboratorioSheet, 8);

            workbook.write(output);
            return new AdminSecurityExportService.ExportedSpreadsheet(
                "movimientos-sistema-" + FILE_DATE_FORMAT.format(fechaInicio) + "-" + FILE_DATE_FORMAT.format(fechaFin) + ".xlsx",
                CONTENT_TYPE,
                output.toByteArray()
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel de movimientos del sistema.", e);
        }
    }

    private List<ConsultaMedica> resolveConsultas(LocalDate fechaInicio, LocalDate fechaFin, Long pacienteId, String doctorLogin) {
        validateDateRange(fechaInicio, fechaFin);

        List<ConsultaMedica> consultas = fetchConsultas(fechaInicio, fechaFin, doctorLogin).stream()
                .filter(consulta -> pacienteId == null || matchesPatient(consulta, pacienteId))
                .sorted(Comparator.comparing(ConsultaMedica::getFechaConsulta).reversed().thenComparing(ConsultaMedica::getId).reversed())
                .toList();

        return consultas;
    }

    private List<ConsultaMedica> fetchConsultas(LocalDate fechaInicio, LocalDate fechaFin, String doctorLogin) {
        if (doctorLogin != null && !doctorLogin.isBlank()) {
            return consultaMedicaRepository.findAllWithReportDetailsByFechaConsultaBetweenAndUserLogin(fechaInicio, fechaFin, doctorLogin);
        }
        return consultaMedicaRepository.findAllWithReportDetailsByFechaConsultaBetween(fechaInicio, fechaFin);
    }

    private List<ResultadoLaboratorio> resolveResultadosLaboratorio(LocalDate fechaInicio, LocalDate fechaFin) {
        return resultadoLaboratorioRepository.findAllWithReportDetailsByFechaExamenBetween(
            fechaInicio,
            fechaFin,
            Sort.by(Sort.Direction.DESC, "fechaExamen").and(Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    private void validateDateRange(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Debe indicar la fecha inicial y final del reporte.");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser menor que la fecha inicial.");
        }
    }

    private boolean matchesPatient(ConsultaMedica consulta, Long pacienteId) {
        return consulta.getExpediente() != null
            && consulta.getExpediente().getPaciente() != null
            && pacienteId.equals(consulta.getExpediente().getPaciente().getId());
    }

    private String resolvePatientLabel(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
            .map(this::fullName)
            .orElse("Paciente " + pacienteId);
    }

    private String patientName(ConsultaMedica consulta) {
        if (consulta.getExpediente() == null || consulta.getExpediente().getPaciente() == null) {
            return "Paciente";
        }
        return fullName(consulta.getExpediente().getPaciente());
    }

    private String patientName(ResultadoLaboratorio resultado) {
        if (resultado.getPaciente() == null) {
            return "Paciente";
        }
        return fullName(resultado.getPaciente());
    }

    private String fullName(Paciente paciente) {
        return ReportTextUtils.fullName(paciente.getNombres(), paciente.getApellidos(), "Paciente");
    }

    private String doctorName(ConsultaMedica consulta) {
        if (consulta.getUser() == null) {
            return "N/D";
        }
        return ReportTextUtils.fullName(
            consulta.getUser().getFirstName(),
            consulta.getUser().getLastName(),
            consulta.getUser().getLogin()
        );
    }

    private String resolveDoctorLabel(String doctorLogin, List<ConsultaMedica> consultas) {
        if (doctorLogin == null || doctorLogin.isBlank()) {
            return "Todos";
        }
        return consultas.stream()
            .filter(consulta -> consulta.getUser() != null && doctorLogin.equals(consulta.getUser().getLogin()))
            .findFirst()
            .map(this::doctorName)
            .orElse(doctorLogin);
    }

    private long countDistinctPatients(List<ConsultaMedica> consultas, List<ResultadoLaboratorio> resultados) {
        return java.util.stream.Stream.concat(
                consultas.stream()
                    .filter(consulta -> consulta.getExpediente() != null && consulta.getExpediente().getPaciente() != null)
                    .map(consulta -> consulta.getExpediente().getPaciente().getId()),
                resultados.stream()
                    .filter(resultado -> resultado.getPaciente() != null)
                    .map(resultado -> resultado.getPaciente().getId())
            )
            .filter(id -> id != null)
            .distinct()
            .count();
    }

    private long countDistinctDoctors(List<ConsultaMedica> consultas) {
        return consultas.stream()
            .filter(consulta -> consulta.getUser() != null && consulta.getUser().getLogin() != null)
            .map(consulta -> consulta.getUser().getLogin())
            .distinct()
            .count();
    }

    private String diagnosticoPrincipal(ConsultaMedica consulta) {
        return consulta.getDiagnosticos().stream()
            .findFirst()
            .map(diagnostico -> {
                String codigo = diagnostico.getCodigoCIE() != null ? "[" + diagnostico.getCodigoCIE() + "] " : "";
                return codigo + ReportTextUtils.defaultText(diagnostico.getDescripcion());
            })
            .orElse("Sin diagnóstico");
    }

    private String diagnosticosTexto(ConsultaMedica consulta) {
        String value = consulta.getDiagnosticos().stream()
            .map(diagnostico -> {
                String codigo = diagnostico.getCodigoCIE() != null ? diagnostico.getCodigoCIE() + " - " : "";
                return codigo + ReportTextUtils.defaultText(diagnostico.getDescripcion());
            })
            .reduce((left, right) -> left + "; " + right)
            .orElse("");
        return value.isBlank() ? "Sin diagnósticos" : value;
    }

    private String recetasTexto(ConsultaMedica consulta) {
        String value = consulta.getRecetas().stream()
            .map(receta -> {
                String medicamento = receta.getMedicamento() != null ? receta.getMedicamento().getNombre() : "Medicamento";
                return medicamento + " - " + ReportTextUtils.defaultText(receta.getDosis()) + ", " + ReportTextUtils.defaultText(receta.getFrecuencia());
            })
            .reduce((left, right) -> left + "; " + right)
            .orElse("");
        return value.isBlank() ? "Sin recetas" : value;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    private void addCoverSheet(XSSFWorkbook workbook, String reportTitle, String subtitle, List<InfoItem> items) {
        var settings = systemSettingsService.getSettings();
        WorkbookStyles styles = createWorkbookStyles(workbook);
        workbook.getProperties().getCoreProperties().setCreator(settings.brandName());
        workbook.getProperties().getCoreProperties().setTitle(settings.applicationName() + " - " + reportTitle);

        Sheet sheet = workbook.createSheet("Información");
        Row brandRow = sheet.createRow(0);
        Cell brandCell = brandRow.createCell(0);
        brandCell.setCellValue(settings.brandName());
        brandCell.setCellStyle(styles.title());
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        Row appRow = sheet.createRow(1);
        Cell appCell = appRow.createCell(0);
        appCell.setCellValue(settings.applicationName());
        appCell.setCellStyle(styles.subtitle());
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

        Row reportRow = sheet.createRow(3);
        writeCell(reportRow, 0, "Reporte", styles.label());
        writeCell(reportRow, 1, reportTitle, styles.body());
        writeCell(reportRow, 2, "Generado", styles.label());
        writeCell(reportRow, 3, formatDate(LocalDate.now()), styles.body());

        Row subtitleRow = sheet.createRow(4);
        writeCell(subtitleRow, 0, "Detalle", styles.label());
        writeCell(subtitleRow, 1, subtitle, styles.body());
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 3));

        int rowIndex = 6;
        Row infoHeader = sheet.createRow(rowIndex++);
        writeCell(infoHeader, 0, "Campo", styles.header());
        writeCell(infoHeader, 1, "Valor", styles.header());
        for (InfoItem item : items) {
            Row row = sheet.createRow(rowIndex++);
            writeCell(row, 0, item.label(), styles.label());
            writeCell(row, 1, item.value(), styles.body());
        }
        for (int index = 0; index < 4; index++) {
            sheet.autoSizeColumn(index);
            sheet.setColumnWidth(index, Math.max(sheet.getColumnWidth(index), 18 * 256));
        }
    }

    private void writeHeader(Row row, String... values) {
        for (int index = 0; index < values.length; index++) {
            writeCell(row, index, values[index]);
        }
    }

    private void writeCell(Row row, int index, String value) {
        row.createCell(index).setCellValue(value == null ? "" : value);
    }

    private void writeCell(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void autoSize(Sheet sheet, int columns) {
        WorkbookStyles styles = createWorkbookStyles(sheet.getWorkbook());
        if (sheet.getLastRowNum() >= 0 && sheet.getRow(0) != null) {
            for (int index = 0; index < columns; index++) {
                Cell cell = sheet.getRow(0).getCell(index);
                if (cell != null) {
                    cell.setCellStyle(styles.header());
                }
            }
            sheet.createFreezePane(0, 1);
            if (sheet.getLastRowNum() > 0) {
                sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, columns - 1));
            }
        }
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            for (int cellIndex = 0; cellIndex < columns; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                if (cell != null && cell.getCellStyle().getIndex() == 0) {
                    cell.setCellStyle(styles.body());
                }
            }
        }
        for (int index = 0; index < columns; index++) {
            sheet.autoSizeColumn(index);
            int width = Math.max(sheet.getColumnWidth(index), 14 * 256);
            sheet.setColumnWidth(index, Math.min(width, 45 * 256));
        }
    }

    private WorkbookStyles createWorkbookStyles(Workbook workbook) {
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        Font subtitleFont = workbook.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeightInPoints((short) 12);
        subtitleFont.setColor(IndexedColors.BLUE_GREY.getIndex());

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelFont.setColor(IndexedColors.BLUE_GREY.getIndex());

        CellStyle title = workbook.createCellStyle();
        title.setFont(titleFont);
        title.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle subtitle = workbook.createCellStyle();
        subtitle.setFont(subtitleFont);
        subtitle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle header = workbook.createCellStyle();
        header.setFont(headerFont);
        header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setAlignment(HorizontalAlignment.CENTER);
        header.setVerticalAlignment(VerticalAlignment.CENTER);
        header.setWrapText(true);
        addThinBorder(header);

        CellStyle label = workbook.createCellStyle();
        label.setFont(labelFont);
        label.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        label.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        label.setWrapText(true);
        addThinBorder(label);

        CellStyle body = workbook.createCellStyle();
        body.setWrapText(true);
        body.setVerticalAlignment(VerticalAlignment.TOP);
        addThinBorder(body);

        return new WorkbookStyles(title, subtitle, header, label, body);
    }

    private void addThinBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private record WorkbookStyles(CellStyle title, CellStyle subtitle, CellStyle header, CellStyle label, CellStyle body) {}
}
