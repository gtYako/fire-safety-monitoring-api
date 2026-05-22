package com.firesafety.service;

import com.firesafety.dto.request.RoomRequest;
import com.firesafety.dto.response.RoomResponse;
import com.firesafety.entity.Building;
import com.firesafety.entity.Room;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.BuildingRepository;
import com.firesafety.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    // CRUD-логика для помещений и привязка каждого помещения к корпусу.
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<RoomResponse> getAll() {
        return roomRepository.findAll().stream().map(mapper::toRoomResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        return mapper.toRoomResponse(find(id));
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        log.info("Creating room {} in building {}", request.getNumber(), request.getBuildingId());

        // Помещение нельзя создать без существующего корпуса.
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building", request.getBuildingId()));

        Room room = Room.builder()
                .number(request.getNumber())
                .floor(request.getFloor())
                .purpose(request.getPurpose())
                .building(building)
                .build();
        return mapper.toRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        log.info("Updating room id={}", id);
        Room room = find(id);
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building", request.getBuildingId()));

        room.setNumber(request.getNumber());
        room.setFloor(request.getFloor());
        room.setPurpose(request.getPurpose());
        room.setBuilding(building);
        return mapper.toRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting room id={}", id);
        find(id);
        roomRepository.deleteById(id);
    }

    private Room find(Long id) {
        // Общий поиск помещения для методов чтения, обновления и удаления.
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }
}
