package com.firesafety.repository;

import com.firesafety.entity.IncidentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentPhotoRepository extends JpaRepository<IncidentPhoto, Long> {
    List<IncidentPhoto> findByIncidentId(Long incidentId);
}
