package com.firesafety.controller;

import com.firesafety.dto.request.AlertStatusRequest;
import com.firesafety.dto.response.AlertResponse;
import com.firesafety.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Fire safety alerts")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAuthority('ALERT_READ')")
    @Operation(summary = "Get all alerts")
    public ResponseEntity<List<AlertResponse>> getAll() {
        return ResponseEntity.ok(alertService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ALERT_READ')")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<AlertResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ALERT_UPDATE')")
    @Operation(summary = "Update alert status")
    public ResponseEntity<AlertResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody AlertStatusRequest request) {
        return ResponseEntity.ok(alertService.updateStatus(id, request));
    }
}
