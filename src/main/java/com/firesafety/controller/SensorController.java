package com.firesafety.controller;

import com.firesafety.dto.request.SensorRequest;
import com.firesafety.dto.response.SensorResponse;
import com.firesafety.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@Tag(name = "Sensors", description = "Sensor management")
@SecurityRequirement(name = "bearerAuth")
public class SensorController {

    private final SensorService sensorService;

    @GetMapping
    @PreAuthorize("hasAuthority('SENSOR_READ')")
    @Operation(summary = "Get all sensors")
    public ResponseEntity<List<SensorResponse>> getAll() {
        return ResponseEntity.ok(sensorService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SENSOR_READ')")
    @Operation(summary = "Get sensor by ID")
    public ResponseEntity<SensorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sensorService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SENSOR_CREATE')")
    @Operation(summary = "Create sensor")
    public ResponseEntity<SensorResponse> create(@Valid @RequestBody SensorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SENSOR_UPDATE')")
    @Operation(summary = "Update sensor")
    public ResponseEntity<SensorResponse> update(@PathVariable Long id, @Valid @RequestBody SensorRequest request) {
        return ResponseEntity.ok(sensorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SENSOR_DELETE')")
    @Operation(summary = "Delete sensor")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sensorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
