package com.firesafety.repository;

import com.firesafety.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByBuildingId(Long buildingId);
    boolean existsByNumberAndBuildingId(String number, Long buildingId);
}
