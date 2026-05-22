package com.firesafety.service;

import com.firesafety.dto.request.SensorReadingRequest;
import com.firesafety.dto.response.SensorReadingResponse;
import com.firesafety.entity.Alert;
import com.firesafety.entity.Sensor;
import com.firesafety.entity.SensorReading;
import com.firesafety.enums.SensorStatus;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.SensorReadingRepository;
import com.firesafety.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorReadingService {

    // Репозиторий хранит показания датчиков в базе данных.
    private final SensorReadingRepository readingRepository;

    // Нужен, чтобы найти датчик по ID перед сохранением нового показания.
    private final SensorRepository sensorRepository;

    // Создаёт тревогу, если новое показание превысило порог датчика.
    private final AlertService alertService;

    // Преобразует Entity-объекты в DTO-ответы для API.
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAll() {
        // Возвращает все сохранённые показания датчиков.
        return readingRepository.findAll().stream().map(mapper::toReadingResponse).collect(Collectors.toList());
    }

    @Transactional
    public SensorReadingResponse create(SensorReadingRequest request) {
        // Находим датчик, для которого пришло новое показание.
        Sensor sensor = sensorRepository.findById(request.getSensorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", request.getSensorId()));

        // Показания принимаются только от активных датчиков.
        if (sensor.getStatus() != SensorStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot add reading to sensor with status: " + sensor.getStatus());
        }

        // Сохраняем само показание в базе данных.
        SensorReading reading = SensorReading.builder()
                .sensor(sensor)
                .value(request.getValue())
                .build();
        reading = readingRepository.save(reading);

        log.info("Reading saved: sensor={}, value={}, threshold={}",
                sensor.getInventoryNumber(), request.getValue(), sensor.getThresholdValue());

        SensorReadingResponse response = mapper.toReadingResponse(reading);

        // Если значение выше порога, создаём тревогу и добавляем её ID в ответ API.
        if (request.getValue() > sensor.getThresholdValue()) {
            log.warn("Threshold exceeded! sensor={}, value={} > threshold={}",
                    sensor.getInventoryNumber(), request.getValue(), sensor.getThresholdValue());
            Alert alert = alertService.createAlert(sensor, reading);
            response.setAlertId(alert.getId());
            response.setExceeded(true);
        }

        return response;
    }
}
