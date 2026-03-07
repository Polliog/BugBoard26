package it.unina.bugboard26.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.opencsv.CSVWriter;

import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Service
@Transactional(readOnly = true)
public class ExportService {

    private final IssueService issueService;

    public ExportService(IssueService issueService) {
        this.issueService = issueService;
    }

    public byte[] exportCsv(List<IssueType> types,
                            List<IssueStatus> statuses,
                            List<IssuePriority> priorities,
                            String assignedToId,
                            Boolean archived,
                            String search) {
        List<Issue> issues = issueService.getFilteredForExport(
                types, statuses, priorities, assignedToId, archived, search
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            String[] header = {"ID", "Titolo", "Tipo", "Priorita", "Stato", "Creato da",
                    "Data creazione", "Archiviata"};
            writer.writeNext(header);

            for (Issue issue : issues) {
                String[] row = {
                        issue.getId(),
                        issue.getTitle(),
                        issue.getType().name(),
                        issue.getPriority() != null ? issue.getPriority().name() : "",
                        issue.getStatus().name(),
                        issue.getCreatedBy() != null ? issue.getCreatedBy().getName() : "",
                        issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "",
                        String.valueOf(issue.isArchived())
                };
                writer.writeNext(row);
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'export CSV", e);
        }
    }

    public byte[] exportPdf(List<IssueType> types,
                            List<IssueStatus> statuses,
                            List<IssuePriority> priorities,
                            String assignedToId,
                            Boolean archived,
                            String search) {

        List<Issue> issues = issueService.getFilteredForExport(
                types, statuses, priorities, assignedToId, archived, search
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(30, 40, 40, 40);

            PdfFont regular = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            PdfFont bold = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

            DeviceRgb primary = new DeviceRgb(30, 58, 138);
            DeviceRgb lightGray = new DeviceRgb(243, 244, 246);
            DeviceRgb mediumGray = new DeviceRgb(107, 114, 128);
            DeviceRgb borderColor = new DeviceRgb(209, 213, 219);

            // --- Header banner ---
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setBorder(Border.NO_BORDER);

            Cell titleCell = new Cell()
                    .add(new Paragraph("BugBoard26").setFont(bold).setFontSize(22).setFontColor(primary))
                    .add(new Paragraph("Report Issue").setFont(regular).setFontSize(13).setFontColor(mediumGray))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(8);
            headerTable.addCell(titleCell);

            String now = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault()).format(Instant.now());
            Cell dateCell = new Cell()
                    .add(new Paragraph("Generato il " + now).setFont(regular).setFontSize(9)
                            .setFontColor(mediumGray).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(issues.size() + " issue totali").setFont(bold).setFontSize(11)
                            .setFontColor(primary).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            headerTable.addCell(dateCell);
            document.add(headerTable);

            // --- Separator line ---
            Table separator = new Table(1).setWidth(UnitValue.createPercentValue(100));
            separator.addCell(new Cell().setHeight(2).setBackgroundColor(primary)
                    .setBorder(Border.NO_BORDER).setPadding(0));
            document.add(separator);
            document.add(new Paragraph("").setMarginBottom(10));

            // --- Summary stats ---
            long todo = issues.stream().filter(i -> i.getStatus() == IssueStatus.TODO).count();
            long inProgress = issues.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count();
            long risolta = issues.stream().filter(i -> i.getStatus() == IssueStatus.RISOLTA).count();
            long chiusa = issues.stream().filter(i -> i.getStatus() == IssueStatus.CHIUSA).count();

            Table statsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}));
            statsTable.setWidth(UnitValue.createPercentValue(100));
            statsTable.setMarginBottom(15);

            addStatCell(statsTable, "Todo", String.valueOf(todo), new DeviceRgb(107, 114, 128), bold, regular);
            addStatCell(statsTable, "In Progress", String.valueOf(inProgress), new DeviceRgb(37, 99, 235), bold, regular);
            addStatCell(statsTable, "Risolta", String.valueOf(risolta), new DeviceRgb(22, 163, 74), bold, regular);
            addStatCell(statsTable, "Chiusa", String.valueOf(chiusa), new DeviceRgb(75, 85, 99), bold, regular);

            document.add(statsTable);

            // --- Data table ---
            float[] colWidths = {35, 12, 12, 12, 14, 15};
            Table table = new Table(UnitValue.createPercentArray(colWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Titolo", "Tipo", "Priorita", "Stato", "Creato da", "Data creazione"};
            for (String h : headers) {
                Cell hCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(new DeviceRgb(255, 255, 255)))
                        .setBackgroundColor(primary)
                        .setPadding(7)
                        .setBorder(Border.NO_BORDER);
                table.addHeaderCell(hCell);
            }

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());

            for (int i = 0; i < issues.size(); i++) {
                Issue issue = issues.get(i);
                DeviceRgb rowBg = (i % 2 == 0) ? new DeviceRgb(255, 255, 255) : lightGray;

                addDataCell(table, issue.getTitle(), regular, 9, rowBg, null, borderColor);
                addDataCell(table, formatType(issue.getType()), regular, 9, rowBg, typeColor(issue.getType()), borderColor);
                addDataCell(table, formatPriority(issue.getPriority()), regular, 9, rowBg, priorityColor(issue.getPriority()), borderColor);
                addDataCell(table, formatStatus(issue.getStatus()), bold, 9, rowBg, statusColor(issue.getStatus()), borderColor);
                addDataCell(table, issue.getCreatedBy() != null ? issue.getCreatedBy().getName() : "-", regular, 9, rowBg, null, borderColor);
                addDataCell(table, issue.getCreatedAt() != null ? dateFmt.format(issue.getCreatedAt()) : "-", regular, 8, rowBg, mediumGray, borderColor);
            }

            document.add(table);

            // --- Footer ---
            document.add(new Paragraph("")
                    .setMarginTop(20));
            document.add(new Paragraph("BugBoard26 — Documento generato automaticamente")
                    .setFont(regular).setFontSize(8).setFontColor(mediumGray)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'export PDF", e);
        }
    }


    public byte[] exportExcel(List<IssueType> types,
                              List<IssueStatus> statuses,
                              List<IssuePriority> priorities,
                              String assignedToId,
                              Boolean archived,
                              String search) {
        List<Issue> issues = issueService.getFilteredForExport(
                types, statuses, priorities, assignedToId, archived, search
        );

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Issue");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] headers = {"ID", "Titolo", "Tipo", "Priorita", "Stato",
                    "Creato da", "Data creazione", "Archiviata"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Issue issue : issues) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(issue.getId());
                row.createCell(1).setCellValue(issue.getTitle());
                row.createCell(2).setCellValue(issue.getType().name());
                row.createCell(3).setCellValue(issue.getPriority() != null ? issue.getPriority().name() : "");
                row.createCell(4).setCellValue(issue.getStatus().name());
                row.createCell(5).setCellValue(issue.getCreatedBy() != null ? issue.getCreatedBy().getName() : "");
                row.createCell(6).setCellValue(issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "");
                row.createCell(7).setCellValue(issue.isArchived());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'export Excel", e);
        }
    }

    private void addStatCell(Table table, String label, String value, DeviceRgb accent,
                             PdfFont bold, PdfFont regular) {
        Cell cell = new Cell()
                .add(new Paragraph(value).setFont(bold).setFontSize(20).setFontColor(accent))
                .add(new Paragraph(label).setFont(regular).setFontSize(9).setFontColor(new DeviceRgb(107, 114, 128)))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(249, 250, 251))
                .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1))
                .setPadding(10);
        table.addCell(cell);
    }

    private void addDataCell(Table table, String text, PdfFont font, float fontSize,
                             DeviceRgb bg, DeviceRgb fontColor, DeviceRgb borderColor) {
        Paragraph p = new Paragraph(text != null ? text : "-").setFont(font).setFontSize(fontSize);
        if (fontColor != null) p.setFontColor(fontColor);

        Cell cell = new Cell().add(p)
                .setBackgroundColor(bg)
                .setPadding(6)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(borderColor, 0.5f));
        table.addCell(cell);
    }

    private String formatType(IssueType type) {
        if (type == null) return "-";
        return switch (type) {
            case BUG -> "Bug";
            case FEATURE -> "Feature";
            case QUESTION -> "Question";
            case DOCUMENTATION -> "Docs";
        };
    }

    private String formatPriority(IssuePriority p) {
        if (p == null) return "-";
        return switch (p) {
            case BASSA -> "Bassa";
            case MEDIA -> "Media";
            case ALTA -> "Alta";
            case CRITICA -> "Critica";
        };
    }

    private String formatStatus(IssueStatus s) {
        if (s == null) return "-";
        return switch (s) {
            case TODO -> "Todo";
            case IN_PROGRESS -> "In Progress";
            case RISOLTA -> "Risolta";
            case CHIUSA -> "Chiusa";
        };
    }

    private DeviceRgb typeColor(IssueType type) {
        if (type == null) return new DeviceRgb(107, 114, 128);
        return switch (type) {
            case BUG -> new DeviceRgb(220, 38, 38);
            case FEATURE -> new DeviceRgb(22, 163, 74);
            case QUESTION -> new DeviceRgb(147, 51, 234);
            case DOCUMENTATION -> new DeviceRgb(37, 99, 235);
        };
    }

    private DeviceRgb priorityColor(IssuePriority p) {
        if (p == null) return new DeviceRgb(107, 114, 128);
        return switch (p) {
            case BASSA -> new DeviceRgb(37, 99, 235);
            case MEDIA -> new DeviceRgb(202, 138, 4);
            case ALTA -> new DeviceRgb(234, 88, 12);
            case CRITICA -> new DeviceRgb(220, 38, 38);
        };
    }

    private DeviceRgb statusColor(IssueStatus s) {
        if (s == null) return new DeviceRgb(107, 114, 128);
        return switch (s) {
            case TODO -> new DeviceRgb(107, 114, 128);
            case IN_PROGRESS -> new DeviceRgb(37, 99, 235);
            case RISOLTA -> new DeviceRgb(22, 163, 74);
            case CHIUSA -> new DeviceRgb(75, 85, 99);
        };
    }
}
