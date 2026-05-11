package com.firesafety.service;

import com.firesafety.dto.request.IncidentRequest;
import com.firesafety.dto.request.IncidentStatusRequest;
import com.firesafety.dto.response.IncidentResponse;
import com.firesafety.entity.Alert;
import com.firesafety.entity.Incident;
import com.firesafety.entity.User;
import com.firesafety.enums.IncidentStatus;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.AlertRepository;
import com.firesafety.repository.IncidentRepository;
import com.firesafety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAll() {
        return incidentRepository.findAll().stream().map(mapper::toIncidentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        return mapper.toIncidentResponse(find(id));
    }

    @Transactional
    public IncidentResponse create(IncidentRequest request) {
        log.info("Creating incident for alert {}", request.getAlertId());
        Alert alert = alertRepository.findById(request.getAlertId())
                .orElseThrow(() -> new ResourceNotFoundException("Alert", request.getAlertId()));

        User responsible = null;
        if (request.getResponsibleUserId() != null) {
            responsible = userRepository.findById(request.getResponsibleUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getResponsibleUserId()));
        }

        Incident incident = Incident.builder()
                .alert(alert)
                .description(request.getDescription())
                .responsibleUser(responsible)
                .status(IncidentStatus.OPEN)
                .build();

        return mapper.toIncidentResponse(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentResponse updateStatus(Long id, IncidentStatusRequest request) {
        log.info("Updating incident {} status to {}", id, request.getStatus());
        Incident incident = find(id);
        incident.setStatus(request.getStatus());
        if (request.getStatus() == IncidentStatus.CLOSED) {
            incident.setClosedAt(LocalDateTime.now());
        }
        return mapper.toIncidentResponse(incidentRepository.save(incident));
    }

    private Incident find(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
    }
}
