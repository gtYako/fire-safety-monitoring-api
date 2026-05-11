package com.firesafety.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentPhotoResponse {
    private Long id;
    private Long incidentId;
    private String originalFileName;
    private String contentType;
    private Long size;
    private LocalDateTime uploadedAt;
}
