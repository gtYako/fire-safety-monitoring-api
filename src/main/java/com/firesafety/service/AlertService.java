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

    private final AlertRepository alertRepository;
    private final EntityMapper mapper;
    private final TelegramService telegramService;

    @Transactional(readOnly = true)
    public List<AlertResponse> getAll() {
        return alertRepository.findAll().stream().map(mapper::toAlertResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(Long id) {
        return mapper.toAlertResponse(find(id));
    }

    @Transactional
    public Alert createAlert(Sensor sensor, SensorReading reading) {
        AlertType alertType = resolveAlertType(sensor);
        String message = buildAlertMessage(sensor, reading);

        log.warn("ALERT CREATED: sensor={}, value={}, threshold={}, type={}",
                sensor.getInventoryNumber(), reading.getValue(), sensor.getThresholdValue(), alertType);

        Alert alert = Alert.builder()
                .sensor(sensor)
                .reading(reading)
                .alertType(alertType)
                .message(message)
                .status(AlertStatus.NEW)
                .build();

        Alert saved = alertRepository.save(alert);
        telegramService.sendAlert(message);
        return saved;
    }

    @Transactional
    public AlertResponse updateStatus(Long id, AlertStatusRequest request) {
        log.info("Updating alert {} status to {}", id, request.getStatus());
        Alert alert = find(id);
        alert.setStatus(request.getStatus());
        if (request.getStatus() == AlertStatus.RESOLVED || request.getStatus() == AlertStatus.FALSE_ALARM) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        return mapper.toAlertResponse(alertRepository.save(alert));
    }

    private AlertType resolveAlertType(Sensor sensor) {
        return switch (sensor.getType()) {
            case SMOKE -> AlertType.SMOKE_DETECTED;
            case TEMPERATURE -> AlertType.HIGH_TEMPERATURE;
            case GAS -> AlertType.GAS_DETECTED;
            case MANUAL_BUTTON -> AlertType.MANUAL_TRIGGER;
        };
    }

    private String buildAlertMessage(Sensor sensor, SensorReading reading) {
        return String.format(
                "🔥 <b>ПОЖАРНАЯ ТРЕВОГА</b>\n" +
                "Датчик: %s\n" +
                "Тип: %s\n" +
                "Помещение: %s, этаж %d\n" +
                "Корпус: %s\n" +
                "Показание: %.2f (порог: %.2f)\n" +
                "Время: %s",
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
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }
}
