package com.firesafety.dto.response;

import com.firesafety.enums.AlertStatus;
import com.firesafety.enums.AlertType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
    private Long id;
    private Long sensorId;
    private String inventoryNumber;
    private String roomNumber;
    private String buildingName;
    private Long readingId;
    private Double readingValue;
    private AlertType alertType;
    private String message;
    private AlertStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
