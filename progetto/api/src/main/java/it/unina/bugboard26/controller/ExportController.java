package it.unina.bugboard26.controller;

import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;
import it.unina.bugboard26.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per l'export delle issue.
 * RF08 - Export CSV/PDF.
 */
@RestController
@RequestMapping("/api/issues/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * RF08 - Esporta le issue nel formato specificato (csv o pdf).
     */
    @GetMapping
    public ResponseEntity<byte[]> export(
            @RequestParam String format,
            @RequestParam(required = false) List<IssueType> type,
            @RequestParam(required = false) List<IssueStatus> status,
            @RequestParam(required = false) List<IssuePriority> priority,
            @RequestParam(required = false) String assignedToId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        Boolean archived = includeArchived ? null : false;

        if ("csv".equalsIgnoreCase(format)) {
            byte[] data = exportService.exportCsv(type, status, priority, assignedToId, archived, search);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=issues.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(data);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] data = exportService.exportPdf(type, status, priority, assignedToId, archived, search);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=issues.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
