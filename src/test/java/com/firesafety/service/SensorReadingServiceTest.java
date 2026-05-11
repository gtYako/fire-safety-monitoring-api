package com.firesafety.service;

import com.firesafety.dto.request.SensorReadingRequest;
import com.firesafety.dto.response.SensorReadingResponse;
import com.firesafety.entity.*;
import com.firesafety.enums.SensorStatus;
import com.firesafety.enums.SensorType;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.SensorReadingRepository;
import com.firesafety.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {

    @Mock
    private SensorReadingRepository readingRepository;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private SensorReadingService readingService;

    private Sensor sensor;
    private Room room;

    @BeforeEach
    void setup() {
        Building building = Building.builder().id(1L).name("Corp A").build();
        room = Room.builder().id(1L).number("101").floor(1).building(building).build();
        sensor = Sensor.builder()
                .id(1L)
                .inventoryNumber("SMOKE-101")
                .type(SensorType.SMOKE)
                .status(SensorStatus.ACTIVE)
                .room(room)
                .thresholdValue(50.0)
                .build();
    }

    @Test
    void createReading_belowThreshold_noAlert() {
        SensorReadingRequest request = new SensorReadingRequest();
        request.setSensorId(1L);
        request.setValue(30.0);

        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
        SensorReading reading = SensorReading.builder().id(1L).sensor(sensor).value(30.0).build();
        when(readingRepository.save(any())).thenReturn(reading);

        SensorReadingResponse mockResponse = SensorReadingResponse.builder()
                .id(1L).sensorId(1L).value(30.0).exceeded(false).build();
        when(mapper.toReadingResponse(reading)).thenReturn(mockResponse);

        SensorReadingResponse result = readingService.create(request);

        assertThat(result.isExceeded()).isFalse();
        assertThat(result.getAlertId()).isNull();
        verify(alertService, never()).createAlert(any(), any());
    }

    @Test
    void createReading_exceedsThreshold_createsAlert() {
        SensorReadingRequest request = new SensorReadingRequest();
        request.setSensorId(1L);
        request.setValue(75.0);

        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
        SensorReading reading = SensorReading.builder().id(1L).sensor(sensor).value(75.0).build();
        when(readingRepository.save(any())).thenReturn(reading);

        SensorReadingResponse mockResponse = SensorReadingResponse.builder()
                .id(1L).sensorId(1L).value(75.0).exceeded(true).build();
        when(mapper.toReadingResponse(reading)).thenReturn(mockResponse);

        Alert alert = Alert.builder().id(10L).build();
        when(alertService.createAlert(any(), any())).thenReturn(alert);

        SensorReadingResponse result = readingService.create(request);

        assertThat(result.getAlertId()).isEqualTo(10L);
        verify(alertService, times(1)).createAlert(any(), any());
    }
}
