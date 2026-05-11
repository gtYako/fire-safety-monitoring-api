package com.firesafety.service;

import com.firesafety.dto.response.ImportLogResponse;
import com.firesafety.entity.ImportLog;
import com.firesafety.entity.Room;
import com.firesafety.entity.Sensor;
import com.firesafety.enums.ImportStatus;
import com.firesafety.enums.SensorStatus;
import com.firesafety.enums.SensorType;
import com.firesafety.exception.ImportException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.ImportLogRepository;
import com.firesafety.repository.RoomRepository;
import com.firesafety.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final SensorRepository sensorRepository;
    private final RoomRepository roomRepository;
    private final ImportLogRepository importLogRepository;
    private final EntityMapper mapper;

    @Transactional
    public ImportLogResponse importSensors(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportException("CSV file is empty");
        }

        String fileName = file.getOriginalFilename();
        int imported = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.toLowerCase().contains("inventorynumber")) {
                    continue;
                }
                if (line.isBlank()) continue;

                try {
                    String[] parts = line.split(",");
                    if (parts.length < 5) {
                        throw new IllegalArgumentException("Expected 5 columns, got " + parts.length);
                    }

                    String inventoryNumber = parts[0].trim();
                    SensorType type = SensorType.valueOf(parts[1].trim().toUpperCase());
                    SensorStatus status = SensorStatus.valueOf(parts[2].trim().toUpperCase());
                    Long roomId = Long.parseLong(parts[3].trim());
                    Double threshold = Double.parseDouble(parts[4].trim());

                    if (sensorRepository.existsByInventoryNumber(inventoryNumber)) {
                        throw new IllegalArgumentException("Duplicate inventory number: " + inventoryNumber);
                    }

                    Room room = roomRepository.findById(roomId)
                            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

                    Sensor sensor = Sensor.builder()
                            .inventoryNumber(inventoryNumber)
                            .type(type)
                            .status(status)
                            .room(room)
                            .thresholdValue(threshold)
                            .build();
                    sensorRepository.save(sensor);
                    imported++;
                    log.debug("Imported sensor: {}", inventoryNumber);
                } catch (Exception e) {
                    failed++;
                    String error = "Line " + lineNumber + ": " + e.getMessage();
                    errors.add(error);
                    log.warn("CSV import error at line {}: {}", lineNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new ImportException("Failed to read CSV file: " + e.getMessage(), e);
        }

        ImportStatus status = failed == 0 ? ImportStatus.SUCCESS
                : (imported == 0 ? ImportStatus.FAILED : ImportStatus.PARTIAL);

        ImportLog importLog = ImportLog.builder()
                .fileName(fileName)
                .importedCount(imported)
                .failedCount(failed)
                .status(status)
                .errors(errors.isEmpty() ? null : String.join("\n", errors))
                .build();

        log.info("CSV import complete: {} imported, {} failed", imported, failed);
        return mapper.toImportLogResponse(importLogRepository.save(importLog));
    }
}
