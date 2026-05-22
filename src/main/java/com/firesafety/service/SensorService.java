package com.firesafety.service;

import com.firesafety.dto.request.SensorRequest;
import com.firesafety.dto.response.SensorResponse;
import com.firesafety.entity.Room;
import com.firesafety.entity.Sensor;
import com.firesafety.enums.SensorStatus;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.RoomRepository;
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
public class SensorService {

    // Управляет датчиками и их привязкой к помещениям.
    private final SensorRepository sensorRepository;
    private final RoomRepository roomRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<SensorResponse> getAll() {
        return sensorRepository.findAll().stream().map(mapper::toSensorResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SensorResponse getById(Long id) {
        return mapper.toSensorResponse(find(id));
    }

    @Transactional
    public SensorResponse create(SensorRequest request) {
        log.info("Creating sensor: {}", request.getInventoryNumber());

        // Инвентарный номер должен быть уникальным, иначе датчики нельзя отличить друг от друга.
        if (sensorRepository.existsByInventoryNumber(request.getInventoryNumber())) {
            throw new IllegalArgumentException("Inventory number already exists: " + request.getInventoryNumber());
        }

        // Датчик всегда должен находиться в существующем помещении.
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.getRoomId()));

        Sensor sensor = Sensor.builder()
                .inventoryNumber(request.getInventoryNumber())
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : SensorStatus.ACTIVE)
                .room(room)
                .thresholdValue(request.getThresholdValue())
                .build();
        return mapper.toSensorResponse(sensorRepository.save(sensor));
    }

    @Transactional
    public SensorResponse update(Long id, SensorRequest request) {
        log.info("Updating sensor id={}", id);
        Sensor sensor = find(id);
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.getRoomId()));

        sensor.setInventoryNumber(request.getInventoryNumber());
        sensor.setType(request.getType());
        if (request.getStatus() != null) sensor.setStatus(request.getStatus());
        sensor.setRoom(room);
        sensor.setThresholdValue(request.getThresholdValue());
        return mapper.toSensorResponse(sensorRepository.save(sensor));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting sensor id={}", id);
        find(id);
        sensorRepository.deleteById(id);
    }

    public Sensor findEntity(Long id) {
        return find(id);
    }

    private Sensor find(Long id) {
        // Общий поиск датчика с единым сообщением об ошибке.
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }
}
