package ni.edu.mney.service.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PdfReportSupport {

    private static final Color BRAND = new Color(2, 132, 197);
    private static final Color BRAND_SOFT = new Color(224, 242, 254);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PdfReportSupport() {}

    public static Document newDocument() {
        Rectangle pageSize = PageSize.A4;
        return new Document(pageSize, 40f, 40f, 42f, 42f);
    }

    public static Fonts fonts() {
        return new Fonts(
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK),
                FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MUTED),
                FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK),
                FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK));
    }

    public static void addHeader(
            Document document,
            Fonts fonts,
            String reportTitle,
            String subtitle,
            String referenceLabel,
            String referenceValue,
            LocalDate generatedDate)
            throws DocumentException {
        PdfPTable header = new PdfPTable(new float[] { 3f, 1.4f });
        header.setWidthPercentage(100);
        header.setSpacingAfter(16f);

        PdfPCell left = createCell(true);
        left.addElement(new Paragraph("ClinData", fonts.title()));
        left.addElement(new Paragraph(subtitle, fonts.muted()));

        PdfPCell right = createCell(true);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph report = new Paragraph(reportTitle, fonts.section());
        report.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(report);
        right.addElement(paragraph(referenceLabel + ": " + referenceValue, fonts.small(), 4f));
        right.addElement(paragraph("Generado: " + formatDate(generatedDate), fonts.small(), 2f));

        header.addCell(left);
        header.addCell(right);
        document.add(header);
    }

    public static void addInfoGrid(Document document, Fonts fonts, List<InfoItem> items) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(14f);
        table.setWidths(new float[] { 1f, 1f });
        for (InfoItem item : items) {
            PdfPCell cell = new PdfPCell();
            cell.setBorderColor(BORDER);
            cell.setBorderWidth(0.8f);
            cell.setBackgroundColor(BRAND_SOFT);
            cell.setPadding(10f);
            cell.addElement(new Paragraph(item.label(), fonts.small()));
            cell.addElement(paragraph(item.value(), fonts.normal(), 4f));
            table.addCell(cell);
        }
        if (items.size() % 2 != 0) {
            table.addCell(createCell(false));
        }
        document.add(table);
    }

    public static void addSectionTitle(Document document, Fonts fonts, String title) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, fonts.section());
        paragraph.setSpacingBefore(4f);
        paragraph.setSpacingAfter(8f);
        document.add(paragraph);
    }

    public static PdfPTable createTable(Fonts fonts, float[] widths, String... headers) throws DocumentException {
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(10f);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, fonts.tableHeader()));
            cell.setBackgroundColor(BRAND);
            cell.setBorderColor(BRAND);
            cell.setPadding(8f);
            table.addCell(cell);
        }
        return table;
    }

    public static PdfPCell createBodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorderColor(BORDER);
        cell.setPadding(8f);
        return cell;
    }

    public static void addEmptyState(Document document, Fonts fonts, String message) throws DocumentException {
        PdfPCell cell = new PdfPCell(new Paragraph(message, fonts.small()));
        cell.setPadding(12f);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(new Color(248, 250, 252));
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.addCell(cell);
        table.setSpacingAfter(10f);
        document.add(table);
    }

    public static void addSignature(Document document, Fonts fonts, String label) throws DocumentException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingBefore(12f);
        document.add(spacer);

        Paragraph signatureLine = new Paragraph("_______________________________", fonts.normal());
        signatureLine.setAlignment(Element.ALIGN_RIGHT);
        document.add(signatureLine);

        Paragraph signatureLabel = new Paragraph(label, fonts.small());
        signatureLabel.setAlignment(Element.ALIGN_RIGHT);
        signatureLabel.setSpacingBefore(4f);
        document.add(signatureLabel);
    }

    public static String formatDate(LocalDate date) {
        return date != null ? DATE_FORMAT.format(date) : "N/D";
    }

    private static Paragraph paragraph(String text, Font font, float spacingBefore) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingBefore(spacingBefore);
        return paragraph;
    }

    private static PdfPCell createCell(boolean borderBottom) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(borderBottom ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
        cell.setBorderColor(BORDER);
        cell.setPaddingBottom(10f);
        return cell;
    }

    public record Fonts(Font title, Font section, Font tableHeader, Font label, Font muted, Font normal, Font small) {}

    public record InfoItem(String label, String value) {}
}
