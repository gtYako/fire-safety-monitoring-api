package com.firesafety.controller;

import com.firesafety.dto.request.BuildingRequest;
import com.firesafety.dto.response.BuildingResponse;
import com.firesafety.service.BuildingService;
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
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@Tag(name = "Buildings", description = "Building management")
@SecurityRequirement(name = "bearerAuth")
public class BuildingController {

    // REST endpoint'ы для справочника корпусов.
    private final BuildingService buildingService;

    @GetMapping
    @PreAuthorize("hasAuthority('BUILDING_READ')")
    @Operation(summary = "Get all buildings")
    public ResponseEntity<List<BuildingResponse>> getAll() {
        return ResponseEntity.ok(buildingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BUILDING_READ')")
    @Operation(summary = "Get building by ID")
    public ResponseEntity<BuildingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BUILDING_MANAGE')")
    @Operation(summary = "Create building")
    public ResponseEntity<BuildingResponse> create(@Valid @RequestBody BuildingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(buildingService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BUILDING_MANAGE')")
    @Operation(summary = "Update building")
    public ResponseEntity<BuildingResponse> update(@PathVariable Long id, @Valid @RequestBody BuildingRequest request) {
        return ResponseEntity.ok(buildingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BUILDING_MANAGE')")
    @Operation(summary = "Delete building")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
