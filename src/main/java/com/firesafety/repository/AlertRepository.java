package com.firesafety.repository;

import com.firesafety.entity.Alert;
import com.firesafety.enums.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(AlertStatus status);

    Page<Alert> findAll(Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<Alert> findByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Alert> findBySensorId(Long sensorId);
}
