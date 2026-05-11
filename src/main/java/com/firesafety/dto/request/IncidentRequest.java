package com.firesafety.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidentRequest {

    @NotNull(message = "Alert ID is required")
    private Long alertId;

    @NotBlank(message = "Description is required")
    private String description;

    private Long responsibleUserId;
}
