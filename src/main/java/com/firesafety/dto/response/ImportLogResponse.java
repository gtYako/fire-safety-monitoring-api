package com.firesafety.dto.response;

import com.firesafety.enums.ImportStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportLogResponse {
    private Long id;
    private String fileName;
    private Integer importedCount;
    private Integer failedCount;
    private LocalDateTime importedAt;
    private ImportStatus status;
    private String errors;
}
