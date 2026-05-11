package com.firesafety.dto.request;

import com.firesafety.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidentStatusRequest {

    @NotNull(message = "Status is required")
    private IncidentStatus status;
}
