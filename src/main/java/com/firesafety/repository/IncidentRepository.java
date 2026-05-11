package com.firesafety.repository;

import com.firesafety.entity.Incident;
import com.firesafety.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByStatus(IncidentStatus status);
    List<Incident> findByResponsibleUserId(Long userId);
}
