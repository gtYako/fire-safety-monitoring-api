package com.firesafety.controller;

import com.firesafety.dto.response.TelegramLogResponse;
import com.firesafety.entity.TelegramLog;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.TelegramLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Tag(name = "Telegram", description = "Telegram notification logs")
@SecurityRequirement(name = "bearerAuth")
public class TelegramController {

    private final TelegramLogRepository telegramLogRepository;
    private final EntityMapper mapper;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Telegram notification logs")
    public ResponseEntity<List<TelegramLogResponse>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<TelegramLogResponse> logs = telegramLogRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt")))
                .stream()
                .map(mapper::toTelegramLogResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }
}
