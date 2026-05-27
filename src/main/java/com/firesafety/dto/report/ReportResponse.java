package com.firesafety.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private String reportType;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private LocalDateTime generatedAt;
    private int totalRows;
    private List<AlertReportRow> rows;
}
