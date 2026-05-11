package com.firesafety.entity;

import com.firesafety.enums.ImportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    @Builder.Default
    private Integer importedCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer failedCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime importedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;

    @Column(columnDefinition = "TEXT")
    private String errors;
}
