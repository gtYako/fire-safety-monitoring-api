package com.firesafety.controller;

import com.firesafety.dto.report.ReportRequest;
import com.firesafety.dto.report.ReportResponse;
import com.firesafety.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "PDF and XLSX report generation")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    // Endpoint'ы возвращают готовые файлы отчётов по тревогам.
    private final ReportService reportService;

    @PostMapping("/alerts/preview")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    @Operation(summary = "Preview alert report rows as JSON")
    public ResponseEntity<ReportResponse> alertsPreview(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.previewAlertReport(request.getDateFrom(), request.getDateTo()));
    }

    @GetMapping("/alerts/pdf")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    @Operation(summary = "Generate PDF report for alerts in a date range")
    public ResponseEntity<byte[]> alertsPdf(
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "End date (ISO format)", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        // PDF отдаётся как attachment, чтобы браузер скачал файл.
        byte[] pdf = reportService.generateAlertPdf(dateFrom, dateTo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alerts.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/alerts/xlsx")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    @Operation(summary = "Generate XLSX report for alerts in a date range")
    public ResponseEntity<byte[]> alertsXlsx(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        // XLSX формируется в памяти и возвращается как Excel-файл.
        byte[] xlsx = reportService.generateAlertXlsx(dateFrom, dateTo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alerts.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}
