package com.firesafety.entity;

import com.firesafety.enums.SensorStatus;
import com.firesafety.enums.SensorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String inventoryNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SensorStatus status = SensorStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime installedAt = LocalDateTime.now();

    @Column(nullable = false)
    private Double thresholdValue;
}
