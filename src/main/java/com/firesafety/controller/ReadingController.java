package com.firesafety.controller;

import com.firesafety.dto.request.SensorReadingRequest;
import com.firesafety.dto.response.SensorReadingResponse;
import com.firesafety.service.SensorReadingService;
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
@RequestMapping("/api/readings")
@RequiredArgsConstructor
@Tag(name = "Readings", description = "Sensor readings management")
@SecurityRequirement(name = "bearerAuth")
public class ReadingController {

    // Сервис содержит основную бизнес-логику работы с показаниями датчиков.
    private final SensorReadingService readingService;

    @GetMapping
    @PreAuthorize("hasAuthority('READING_READ')")
    @Operation(summary = "Get all sensor readings")
    public ResponseEntity<List<SensorReadingResponse>> getAll() {
        // GET /api/readings возвращает список всех показаний.
        return ResponseEntity.ok(readingService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('READING_CREATE')")
    @Operation(summary = "Create sensor reading. Automatically creates alert if threshold is exceeded.")
    public ResponseEntity<SensorReadingResponse> create(@Valid @RequestBody SensorReadingRequest request) {
        // POST /api/readings принимает sensorId и value.
        // Если value выше порога датчика, сервис автоматически создаст тревогу.
        return ResponseEntity.status(HttpStatus.CREATED).body(readingService.create(request));
    }
}
