package com.firesafety.service;

import com.firesafety.dto.response.ImportLogResponse;
import com.firesafety.entity.Building;
import com.firesafety.entity.ImportLog;
import com.firesafety.entity.Room;
import com.firesafety.enums.ImportStatus;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.ImportLogRepository;
import com.firesafety.repository.RoomRepository;
import com.firesafety.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ImportLogRepository importLogRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private CsvImportService csvImportService;

    @Test
    void importSensors_validCsv_success() {
        String csv = "inventoryNumber,type,status,roomId,thresholdValue\n" +
                     "TEST-001,SMOKE,ACTIVE,1,50.0\n" +
                     "TEST-002,TEMPERATURE,ACTIVE,1,60.0\n";

        MockMultipartFile file = new MockMultipartFile("file", "sensors.csv",
                "text/csv", csv.getBytes());

        Building building = Building.builder().id(1L).name("Test").build();
        Room room = Room.builder().id(1L).number("101").building(building).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(sensorRepository.existsByInventoryNumber(any())).thenReturn(false);
        when(sensorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ImportLog log = ImportLog.builder().id(1L).importedCount(2).failedCount(0)
                .status(ImportStatus.SUCCESS).fileName("sensors.csv").build();
        when(importLogRepository.save(any())).thenReturn(log);
        when(mapper.toImportLogResponse(any())).thenReturn(
                ImportLogResponse.builder().importedCount(2).failedCount(0).status(ImportStatus.SUCCESS).build()
        );

        ImportLogResponse result = csvImportService.importSensors(file);

        assertThat(result.getImportedCount()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getStatus()).isEqualTo(ImportStatus.SUCCESS);
    }
}
