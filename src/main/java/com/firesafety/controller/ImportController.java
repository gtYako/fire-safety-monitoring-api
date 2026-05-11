package com.firesafety.controller;

import com.firesafety.dto.response.ImportLogResponse;
import com.firesafety.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@Tag(name = "Import", description = "CSV data import")
@SecurityRequirement(name = "bearerAuth")
public class ImportController {

    private final CsvImportService csvImportService;

    @PostMapping(value = "/sensors/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('IMPORT_DATA')")
    @Operation(summary = "Import sensors from CSV file",
               description = "CSV format: inventoryNumber,type,status,roomId,thresholdValue")
    public ResponseEntity<ImportLogResponse> importSensors(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvImportService.importSensors(file));
    }
}
