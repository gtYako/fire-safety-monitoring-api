package com.firesafety.repository;

import com.firesafety.entity.Sensor;
import com.firesafety.enums.SensorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByInventoryNumber(String inventoryNumber);
    boolean existsByInventoryNumber(String inventoryNumber);
    List<Sensor> findByRoomId(Long roomId);
    List<Sensor> findByStatus(SensorStatus status);
}
