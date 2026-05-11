package com.firesafety.dto.response;

import com.firesafety.enums.IncidentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private Long id;
    private Long alertId;
    private String description;
    private Long responsibleUserId;
    private String responsibleUserName;
    private IncidentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private List<IncidentPhotoResponse> photos;
}
