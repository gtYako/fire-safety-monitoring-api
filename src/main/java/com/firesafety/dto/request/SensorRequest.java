package com.firesafety.dto.request;

import com.firesafety.enums.SensorStatus;
import com.firesafety.enums.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SensorRequest {

    @NotBlank(message = "Inventory number is required")
    private String inventoryNumber;

    @NotNull(message = "Sensor type is required")
    private SensorType type;

    private SensorStatus status;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Threshold value is required")
    @Positive(message = "Threshold value must be positive")
    private Double thresholdValue;
}
