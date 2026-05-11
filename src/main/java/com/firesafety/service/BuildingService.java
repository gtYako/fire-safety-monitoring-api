package com.firesafety.service;

import com.firesafety.dto.request.BuildingRequest;
import com.firesafety.dto.response.BuildingResponse;
import com.firesafety.entity.Building;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<BuildingResponse> getAll() {
        log.debug("Fetching all buildings");
        return buildingRepository.findAll().stream().map(mapper::toBuildingResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BuildingResponse getById(Long id) {
        return mapper.toBuildingResponse(find(id));
    }

    @Transactional
    public BuildingResponse create(BuildingRequest request) {
        log.info("Creating building: {}", request.getName());
        Building building = Building.builder()
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .build();
        return mapper.toBuildingResponse(buildingRepository.save(building));
    }

    @Transactional
    public BuildingResponse update(Long id, BuildingRequest request) {
        log.info("Updating building id={}", id);
        Building building = find(id);
        building.setName(request.getName());
        building.setAddress(request.getAddress());
        building.setDescription(request.getDescription());
        return mapper.toBuildingResponse(buildingRepository.save(building));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting building id={}", id);
        find(id);
        buildingRepository.deleteById(id);
    }

    private Building find(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", id));
    }
}
