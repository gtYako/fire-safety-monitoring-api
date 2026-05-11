package com.firesafety.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuildingRequest {

    @NotBlank(message = "Building name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String description;
}
