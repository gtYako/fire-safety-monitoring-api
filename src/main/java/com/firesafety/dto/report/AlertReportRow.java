package com.firesafety.dto.report;

import com.firesafety.enums.AlertStatus;
import com.firesafety.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertReportRow {
    private Long alertId;
    private String inventoryNumber;
    private String roomNumber;
    private String buildingName;
    private AlertType alertType;
    private AlertStatus status;
    private Double value;
    private Double threshold;
    private String message;
    private LocalDateTime createdAt;
}
