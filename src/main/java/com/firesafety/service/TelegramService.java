package com.firesafety.service;

import com.firesafety.entity.TelegramLog;
import com.firesafety.enums.TelegramLogStatus;
import com.firesafety.repository.TelegramLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final TelegramLogRepository telegramLogRepository;

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.chat-id:}")
    private String chatId;

    @Async
    public void sendAlert(String message) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.warn("Telegram is not configured (bot-token or chat-id missing). Skipping notification.");
            saveTelegramLog(message, TelegramLogStatus.SKIPPED, "Telegram not configured");
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = Map.of(
                    "chat_id", chatId,
                    "text", message,
                    "parse_mode", "HTML"
            );
            restTemplate.postForObject(url, body, String.class);
            log.info("Telegram alert sent: {}", message.substring(0, Math.min(50, message.length())));
            saveTelegramLog(message, TelegramLogStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
            saveTelegramLog(message, TelegramLogStatus.FAILED, e.getMessage());
        }
    }

    private void saveTelegramLog(String message, TelegramLogStatus status, String error) {
        try {
            TelegramLog log = TelegramLog.builder()
                    .message(message)
                    .status(status)
                    .errorText(error)
                    .build();
            telegramLogRepository.save(log);
        } catch (Exception ex) {
            log.error("Failed to save telegram log: {}", ex.getMessage());
        }
    }
}
