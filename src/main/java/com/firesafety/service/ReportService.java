package com.firesafety.service;

import com.firesafety.entity.Alert;
import com.firesafety.entity.ReportLog;
import com.firesafety.entity.User;
import com.firesafety.enums.ReportType;
import com.firesafety.exception.ReportGenerationException;
import com.firesafety.repository.AlertRepository;
import com.firesafety.repository.ReportLogRepository;
import com.firesafety.repository.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    // Единый формат даты для PDF/XLSX отчётов.
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Сервис формирует отчёты по тревогам за выбранный период.
    private final AlertRepository alertRepository;
    private final ReportLogRepository reportLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public byte[] generateAlertPdf(LocalDateTime from, LocalDateTime to) {
        log.info("Generating PDF alert report from {} to {}", from, to);

        // Берём только тревоги, которые попали в заданный интервал.
        List<Alert> alerts = alertRepository.findByPeriod(from, to);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // OpenPDF пишет документ в память, после чего байты возвращаются клиенту.
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Fire Safety Alert Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Period: " + from.format(FMT) + " — " + to.format(FMT)));
            document.add(new Paragraph("Total alerts: " + alerts.size()));
            document.add(new Paragraph(" "));

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"ID", "Sensor", "Type", "Status", "Message", "Created At"};
            for (String h : headers) {
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new Phrase(h,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
                cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
                table.addCell(cell);
            }

            for (Alert a : alerts) {
                table.addCell(String.valueOf(a.getId()));
                table.addCell(a.getSensor().getInventoryNumber());
                table.addCell(a.getAlertType().name());
                table.addCell(a.getStatus().name());
                String msg = a.getMessage() != null ? a.getMessage().substring(0, Math.min(50, a.getMessage().length())) : "";
                table.addCell(msg);
                table.addCell(a.getCreatedAt().format(FMT));
            }

            document.add(table);
            document.close();

            String fileName = "alerts_" + System.currentTimeMillis() + ".pdf";
            saveReportLog(ReportType.ALERT_PDF, fileName);
            log.info("PDF report generated: {} alerts", alerts.size());
            return out.toByteArray();
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    @Transactional
    public byte[] generateAlertXlsx(LocalDateTime from, LocalDateTime to) {
        log.info("Generating XLSX alert report from {} to {}", from, to);

        // Для Excel используется тот же набор тревог, что и для PDF.
        List<Alert> alerts = alertRepository.findByPeriod(from, to);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Alerts");

            // Выделяем строку заголовков, чтобы таблицу было удобно читать.
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Sensor", "Room", "Building", "Alert Type", "Status", "Value", "Threshold", "Message", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Alert a : alerts) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(a.getId());
                row.createCell(1).setCellValue(a.getSensor().getInventoryNumber());
                row.createCell(2).setCellValue(a.getSensor().getRoom().getNumber());
                row.createCell(3).setCellValue(a.getSensor().getRoom().getBuilding().getName());
                row.createCell(4).setCellValue(a.getAlertType().name());
                row.createCell(5).setCellValue(a.getStatus().name());
                row.createCell(6).setCellValue(a.getReading() != null ? a.getReading().getValue() : 0);
                row.createCell(7).setCellValue(a.getSensor().getThresholdValue());
                row.createCell(8).setCellValue(a.getMessage() != null ? a.getMessage() : "");
                row.createCell(9).setCellValue(a.getCreatedAt().format(FMT));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            String fileName = "alerts_" + System.currentTimeMillis() + ".xlsx";
            saveReportLog(ReportType.ALERT_XLSX, fileName);
            log.info("XLSX report generated: {} alerts", alerts.size());
            return out.toByteArray();
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to generate XLSX: " + e.getMessage(), e);
        }
    }

    private void saveReportLog(ReportType type, String fileName) {
        try {
            // Журнал отчётов показывает, кто и когда формировал файл.
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username).orElse(null);
            ReportLog log = ReportLog.builder()
                    .reportType(type)
                    .fileName(fileName)
                    .generatedBy(user)
                    .build();
            reportLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save report log: {}", e.getMessage());
        }
    }
}
