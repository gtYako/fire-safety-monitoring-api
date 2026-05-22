package com.firesafety.mapper;

import com.firesafety.dto.response.*;
import com.firesafety.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EntityMapper {

    // Маппер отделяет внутренние Entity от DTO, которые возвращаются клиенту.
    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    public BuildingResponse toBuildingResponse(Building b) {
        return BuildingResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .address(b.getAddress())
                .description(b.getDescription())
                .roomCount(b.getRooms().size())
                .build();
    }

    public RoomResponse toRoomResponse(Room r) {
        return RoomResponse.builder()
                .id(r.getId())
                .number(r.getNumber())
                .floor(r.getFloor())
                .purpose(r.getPurpose())
                .buildingId(r.getBuilding().getId())
                .buildingName(r.getBuilding().getName())
                .sensorCount(r.getSensors().size())
                .build();
    }

    public SensorResponse toSensorResponse(Sensor s) {
        return SensorResponse.builder()
                .id(s.getId())
                .inventoryNumber(s.getInventoryNumber())
                .type(s.getType())
                .status(s.getStatus())
                .roomId(s.getRoom().getId())
                .roomNumber(s.getRoom().getNumber())
                .buildingName(s.getRoom().getBuilding().getName())
                .installedAt(s.getInstalledAt())
                .thresholdValue(s.getThresholdValue())
                .build();
    }

    public SensorReadingResponse toReadingResponse(SensorReading r) {
        return SensorReadingResponse.builder()
                .id(r.getId())
                .sensorId(r.getSensor().getId())
                .inventoryNumber(r.getSensor().getInventoryNumber())
                .value(r.getValue())
                .thresholdValue(r.getSensor().getThresholdValue())
                // Флаг exceeded сразу показывает клиенту, было ли превышение порога.
                .exceeded(r.getValue() > r.getSensor().getThresholdValue())
                .measuredAt(r.getMeasuredAt())
                .build();
    }

    public AlertResponse toAlertResponse(Alert a) {
        return AlertResponse.builder()
                .id(a.getId())
                .sensorId(a.getSensor().getId())
                .inventoryNumber(a.getSensor().getInventoryNumber())
                .roomNumber(a.getSensor().getRoom().getNumber())
                .buildingName(a.getSensor().getRoom().getBuilding().getName())
                // reading может быть null для вручную созданных или восстановленных записей.
                .readingId(a.getReading() != null ? a.getReading().getId() : null)
                .readingValue(a.getReading() != null ? a.getReading().getValue() : null)
                .alertType(a.getAlertType())
                .message(a.getMessage())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .resolvedAt(a.getResolvedAt())
                .build();
    }

    public IncidentResponse toIncidentResponse(Incident i) {
        return IncidentResponse.builder()
                .id(i.getId())
                .alertId(i.getAlert().getId())
                .description(i.getDescription())
                .responsibleUserId(i.getResponsibleUser() != null ? i.getResponsibleUser().getId() : null)
                .responsibleUserName(i.getResponsibleUser() != null ? i.getResponsibleUser().getUsername() : null)
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .closedAt(i.getClosedAt())
                // Фото инцидента вкладываются сразу, чтобы клиент видел полную карточку.
                .photos(i.getPhotos().stream().map(this::toPhotoResponse).collect(Collectors.toList()))
                .build();
    }

    public IncidentPhotoResponse toPhotoResponse(IncidentPhoto p) {
        return IncidentPhotoResponse.builder()
                .id(p.getId())
                .incidentId(p.getIncident().getId())
                .originalFileName(p.getOriginalFileName())
                .contentType(p.getContentType())
                .size(p.getSize())
                .uploadedAt(p.getUploadedAt())
                .build();
    }

    public TelegramLogResponse toTelegramLogResponse(TelegramLog t) {
        return TelegramLogResponse.builder()
                .id(t.getId())
                .message(t.getMessage())
                .status(t.getStatus())
                .sentAt(t.getSentAt())
                .errorText(t.getErrorText())
                .build();
    }

    public ImportLogResponse toImportLogResponse(ImportLog l) {
        return ImportLogResponse.builder()
                .id(l.getId())
                .fileName(l.getFileName())
                .importedCount(l.getImportedCount())
                .failedCount(l.getFailedCount())
                .importedAt(l.getImportedAt())
                .status(l.getStatus())
                .errors(l.getErrors())
                .build();
    }
}
