package com.firesafety.repository;

import com.firesafety.entity.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    Page<SensorReading> findBySensorId(Long sensorId, Pageable pageable);

    @Query("SELECT r FROM SensorReading r WHERE r.measuredAt BETWEEN :from AND :to ORDER BY r.measuredAt DESC")
    List<SensorReading> findByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
