package com.firesafety.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SensorReadingRequest {

    @NotNull(message = "Sensor ID is required")
    private Long sensorId;

    @NotNull(message = "Value is required")
    private Double value;
}
