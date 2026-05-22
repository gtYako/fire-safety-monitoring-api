package com.firesafety.controller;

import com.firesafety.dto.response.IncidentPhotoResponse;
import com.firesafety.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload for incidents")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    // Загружает фотографии к инцидентам через multipart/form-data.
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/{incidentId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('FILE_UPLOAD')")
    @Operation(summary = "Upload photo to incident (JPEG/PNG/GIF/WebP, max 10MB)")
    public ResponseEntity<IncidentPhotoResponse> uploadPhoto(
            @PathVariable Long incidentId,
            @RequestParam("file") MultipartFile file) {
        // Файл сохраняется на диск, а информация о нём привязывается к инциденту.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileStorageService.savePhoto(incidentId, file));
    }
}
