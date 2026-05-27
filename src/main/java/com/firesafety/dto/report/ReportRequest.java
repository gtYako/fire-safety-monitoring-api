package com.firesafety.dto.report;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportRequest {

    @NotNull(message = "dateFrom is required")
    private LocalDateTime dateFrom;

    @NotNull(message = "dateTo is required")
    private LocalDateTime dateTo;
}
