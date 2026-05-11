package com.firesafety.repository;

import com.firesafety.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    boolean existsByName(String name);
}
