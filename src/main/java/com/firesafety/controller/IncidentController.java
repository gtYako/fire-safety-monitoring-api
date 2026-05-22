package com.firesafety.controller;

import com.firesafety.dto.request.IncidentRequest;
import com.firesafety.dto.request.IncidentStatusRequest;
import com.firesafety.dto.response.IncidentResponse;
import com.firesafety.service.IncidentService;
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
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident management")
@SecurityRequirement(name = "bearerAuth")
public class IncidentController {

    // REST endpoint'ы для создания инцидентов из тревог и ведения их статусов.
    private final IncidentService incidentService;

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    @Operation(summary = "Get all incidents")
    public ResponseEntity<List<IncidentResponse>> getAll() {
        return ResponseEntity.ok(incidentService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<IncidentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INCIDENT_UPDATE')")
    @Operation(summary = "Create incident from alert")
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentRequest request) {
        // Инцидент создаётся вручную на основе уже существующей тревоги.
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('INCIDENT_UPDATE')")
    @Operation(summary = "Update incident status")
    public ResponseEntity<IncidentResponse> updateStatus(@PathVariable Long id,
                                                          @Valid @RequestBody IncidentStatusRequest request) {
        return ResponseEntity.ok(incidentService.updateStatus(id, request));
    }
}
