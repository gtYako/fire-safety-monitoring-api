package com.firesafety.dto.response;

import com.firesafety.enums.TelegramLogStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramLogResponse {
    private Long id;
    private String message;
    private TelegramLogStatus status;
    private LocalDateTime sentAt;
    private String errorText;
}
