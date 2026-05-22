package com.firesafety.service;

import com.firesafety.dto.request.AlertStatusRequest;
import com.firesafety.dto.response.AlertResponse;
import com.firesafety.entity.Alert;
import com.firesafety.entity.Sensor;
import com.firesafety.entity.SensorReading;
import com.firesafety.enums.AlertStatus;
import com.firesafety.enums.AlertType;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    // Репозиторий сохраняет тревоги и загружает их из базы данных.
    private final AlertRepository alertRepository;

    // Маппер преобразует Entity в DTO, которые возвращаются из REST API.
    private final EntityMapper mapper;

    // Сервис отправляет текст тревоги в Telegram.
    private final TelegramService telegramService;

    @Transactional(readOnly = true)
    public List<AlertResponse> getAll() {
        // Возвращает список всех тревог.
        return alertRepository.findAll().stream().map(mapper::toAlertResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(Long id) {
        // Возвращает одну тревогу по ID.
        return mapper.toAlertResponse(find(id));
    }

    @Transactional
    public Alert createAlert(Sensor sensor, SensorReading reading) {
        // Определяем тип тревоги по типу датчика.
        AlertType alertType = resolveAlertType(sensor);

        // Собираем текст, который сохранится в базе и уйдёт в Telegram.
        String message = buildTelegramAlertMessage(sensor, reading);

        log.warn("ALERT CREATED: sensor={}, value={}, threshold={}, type={}",
                sensor.getInventoryNumber(), reading.getValue(), sensor.getThresholdValue(), alertType);

        // Создаём новую тревогу со статусом NEW.
        Alert alert = Alert.builder()
                .sensor(sensor)
                .reading(reading)
                .alertType(alertType)
                .message(message)
                .status(AlertStatus.NEW)
                .build();

        Alert saved = alertRepository.save(alert);

        // Telegram отправляется после сохранения тревоги, чтобы событие не потерялось.
        telegramService.sendAlert(message);
        return saved;
    }

    @Transactional
    public AlertResponse updateStatus(Long id, AlertStatusRequest request) {
        log.info("Updating alert {} status to {}", id, request.getStatus());
        Alert alert = find(id);

        // Меняем статус тревоги: например NEW -> RESOLVED или FALSE_ALARM.
        alert.setStatus(request.getStatus());

        // Для закрытых тревог фиксируем время завершения.
        if (request.getStatus() == AlertStatus.RESOLVED || request.getStatus() == AlertStatus.FALSE_ALARM) {
            alert.setResolvedAt(LocalDateTime.now());
        }

        return mapper.toAlertResponse(alertRepository.save(alert));
    }

    private AlertType resolveAlertType(Sensor sensor) {
        // Тип тревоги зависит от типа датчика, который превысил порог.
        return switch (sensor.getType()) {
            case SMOKE -> AlertType.SMOKE_DETECTED;
            case TEMPERATURE -> AlertType.HIGH_TEMPERATURE;
            case GAS -> AlertType.GAS_DETECTED;
            case MANUAL_BUTTON -> AlertType.MANUAL_TRIGGER;
        };
    }

    private String buildTelegramAlertMessage(Sensor sensor, SensorReading reading) {
        // Unicode-escape защищает русский текст от проблем кодировки в Windows batch/консоли.
        return String.format(
                "\uD83D\uDD25 <b>\u041F\u041E\u0416\u0410\u0420\u041D\u0410\u042F \u0422\u0420\u0415\u0412\u041E\u0413\u0410</b>\n" +
                "\u0414\u0430\u0442\u0447\u0438\u043A: %s\n" +
                "\u0422\u0438\u043F: %s\n" +
                "\u041F\u043E\u043C\u0435\u0449\u0435\u043D\u0438\u0435: %s, \u044D\u0442\u0430\u0436 %d\n" +
                "\u041A\u043E\u0440\u043F\u0443\u0441: %s\n" +
                "\u041F\u043E\u043A\u0430\u0437\u0430\u043D\u0438\u0435: %.2f (\u043F\u043E\u0440\u043E\u0433: %.2f)\n" +
                "\u0412\u0440\u0435\u043C\u044F: %s",
                sensor.getInventoryNumber(),
                sensor.getType(),
                sensor.getRoom().getNumber(),
                sensor.getRoom().getFloor(),
                sensor.getRoom().getBuilding().getName(),
                reading.getValue(),
                sensor.getThresholdValue(),
                reading.getMeasuredAt()
        );
    }

    private Alert find(Long id) {
        // Общий поиск тревоги: если ID неверный, API вернёт понятную ошибку 404.
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }
}
