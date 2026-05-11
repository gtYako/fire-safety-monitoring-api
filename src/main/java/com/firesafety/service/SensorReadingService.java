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

    private final SensorReadingRepository readingRepository;
    private final SensorRepository sensorRepository;
    private final AlertService alertService;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAll() {
        return readingRepository.findAll().stream().map(mapper::toReadingResponse).collect(Collectors.toList());
    }

    @Transactional
    public SensorReadingResponse create(SensorReadingRequest request) {
        Sensor sensor = sensorRepository.findById(request.getSensorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", request.getSensorId()));

        if (sensor.getStatus() != SensorStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot add reading to sensor with status: " + sensor.getStatus());
        }

        SensorReading reading = SensorReading.builder()
                .sensor(sensor)
                .value(request.getValue())
                .build();
        reading = readingRepository.save(reading);

        log.info("Reading saved: sensor={}, value={}, threshold={}",
                sensor.getInventoryNumber(), request.getValue(), sensor.getThresholdValue());

        SensorReadingResponse response = mapper.toReadingResponse(reading);

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
