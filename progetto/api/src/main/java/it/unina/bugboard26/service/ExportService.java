package it.unina.bugboard26.service;

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
import java.util.List;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

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
                    "Assegnato a", "Data creazione", "Archiviata"};
            writer.writeNext(header);

            for (Issue issue : issues) {
                String[] row = {
                        issue.getId(),
                        issue.getTitle(),
                        issue.getType().name(),
                        issue.getPriority() != null ? issue.getPriority().name() : "",
                        issue.getStatus().name(),
                        issue.getCreatedBy() != null ? issue.getCreatedBy().getName() : "",
                        issue.getAssignedTo() != null ? issue.getAssignedTo().getName() : "",
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
            Document document = new Document(pdfDoc);

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            document.add(new Paragraph("BugBoard26 - Report Issue")
                    .setFontSize(18)
                    .setFont(bold));

            document.add(new Paragraph("Totale issue: " + issues.size())
                    .setFontSize(12));

            Table table = new Table(7);
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Titolo", "Tipo", "Priorita", "Stato", "Creato da", "Assegnato a", "Data"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(bold)));
            }

            for (Issue issue : issues) {
                table.addCell(issue.getTitle());
                table.addCell(issue.getType().name());
                table.addCell(issue.getPriority() != null ? issue.getPriority().name() : "-");
                table.addCell(issue.getStatus().name());
                table.addCell(issue.getCreatedBy() != null ? issue.getCreatedBy().getName() : "-");
                table.addCell(issue.getAssignedTo() != null ? issue.getAssignedTo().getName() : "-");
                table.addCell(issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "-");
            }

            document.add(table);
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
                    "Creato da", "Assegnato a", "Data creazione", "Archiviata"};
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
                row.createCell(6).setCellValue(issue.getAssignedTo() != null ? issue.getAssignedTo().getName() : "");
                row.createCell(7).setCellValue(issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "");
                row.createCell(8).setCellValue(issue.isArchived());
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
}
