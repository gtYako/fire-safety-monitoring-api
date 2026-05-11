package com.firesafety.dto.response;

import com.firesafety.enums.SensorStatus;
import com.firesafety.enums.SensorType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorResponse {
    private Long id;
    private String inventoryNumber;
    private SensorType type;
    private SensorStatus status;
    private Long roomId;
    private String roomNumber;
    private String buildingName;
    private LocalDateTime installedAt;
    private Double thresholdValue;
}
