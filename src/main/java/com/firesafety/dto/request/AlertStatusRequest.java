package com.firesafety.dto.request;

import com.firesafety.enums.AlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertStatusRequest {

    @NotNull(message = "Status is required")
    private AlertStatus status;

    private String comment;
}
