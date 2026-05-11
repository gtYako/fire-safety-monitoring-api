package com.firesafety.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorReadingResponse {
    private Long id;
    private Long sensorId;
    private String inventoryNumber;
    private Double value;
    private Double thresholdValue;
    private boolean exceeded;
    private LocalDateTime measuredAt;
    private Long alertId;
}
