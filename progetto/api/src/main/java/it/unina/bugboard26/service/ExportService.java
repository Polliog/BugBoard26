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

/**
 * Servizio per l'export delle issue in CSV e PDF.
 * RF08 - Export CSV/PDF.
 */
@Service
@Transactional(readOnly = true)
public class ExportService {

    private final IssueService issueService;

    public ExportService(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * RF08 - Esporta le issue in formato CSV.
     */
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

            // Header
            String[] header = {"ID", "Titolo", "Tipo", "Priorita", "Stato", "Creato da",
                    "Assegnato a", "Data creazione", "Archiviata"};
            writer.writeNext(header);

            // Dati
            for (Issue issue : issues) {
                String[] row = {
                        issue.getId(),
                        issue.getTitle(),
                        issue.getType().name(),
                        issue.getPriority() != null ? issue.getPriority().name() : "",
                        issue.getStatus().name(),
                        issue.getCreatedBy().getName(),
                        issue.getAssignedTo() != null ? issue.getAssignedTo().getName() : "",
                        issue.getCreatedAt().toString(),
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

    /**
     * RF08 - Esporta le issue in formato PDF.
     */
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
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc =
                    new com.itextpdf.kernel.pdf.PdfDocument(new com.itextpdf.kernel.pdf.PdfWriter(baos));
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);

            // Titolo
            document.add(new com.itextpdf.layout.element.Paragraph("BugBoard26 - Report Issue")
                    .setFontSize(18)
                    .setBold());

            document.add(new com.itextpdf.layout.element.Paragraph("Totale issue: " + issues.size())
                    .setFontSize(12));

            // Tabella
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(7);
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

            // Header tabella
            String[] headers = {"Titolo", "Tipo", "Priorita", "Stato", "Creato da", "Assegnato a", "Data"};
            for (String h : headers) {
                table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                        .add(new com.itextpdf.layout.element.Paragraph(h).setBold()));
            }

            // Righe
            for (Issue issue : issues) {
                table.addCell(issue.getTitle());
                table.addCell(issue.getType().name());
                table.addCell(issue.getPriority() != null ? issue.getPriority().name() : "-");
                table.addCell(issue.getStatus().name());
                table.addCell(issue.getCreatedBy().getName());
                table.addCell(issue.getAssignedTo() != null ? issue.getAssignedTo().getName() : "-");
                table.addCell(issue.getCreatedAt().toString());
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'export PDF", e);
        }
    }
}
