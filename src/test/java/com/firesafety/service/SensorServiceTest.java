package com.firesafety.service;

import com.firesafety.dto.request.SensorRequest;
import com.firesafety.dto.response.SensorResponse;
import com.firesafety.entity.*;
import com.firesafety.enums.SensorStatus;
import com.firesafety.enums.SensorType;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.RoomRepository;
import com.firesafety.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private SensorService sensorService;

    @Test
    void createSensor_success() {
        SensorRequest request = new SensorRequest();
        request.setInventoryNumber("SMOKE-001");
        request.setType(SensorType.SMOKE);
        request.setRoomId(1L);
        request.setThresholdValue(50.0);

        Building building = Building.builder().id(1L).name("Corp A").build();
        Room room = Room.builder().id(1L).number("101").building(building).build();

        when(sensorRepository.existsByInventoryNumber("SMOKE-001")).thenReturn(false);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Sensor sensor = Sensor.builder().id(1L).inventoryNumber("SMOKE-001").type(SensorType.SMOKE)
                .status(SensorStatus.ACTIVE).room(room).thresholdValue(50.0).build();
        when(sensorRepository.save(any())).thenReturn(sensor);

        SensorResponse response = SensorResponse.builder().id(1L).inventoryNumber("SMOKE-001").build();
        when(mapper.toSensorResponse(sensor)).thenReturn(response);

        SensorResponse result = sensorService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getInventoryNumber()).isEqualTo("SMOKE-001");
    }

    @Test
    void getById_notFound_throwsException() {
        when(sensorRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sensorService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createSensor_duplicateInventoryNumber_throwsException() {
        SensorRequest request = new SensorRequest();
        request.setInventoryNumber("SMOKE-001");
        request.setRoomId(1L);
        request.setThresholdValue(50.0);

        when(sensorRepository.existsByInventoryNumber("SMOKE-001")).thenReturn(true);

        assertThatThrownBy(() -> sensorService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }
}
