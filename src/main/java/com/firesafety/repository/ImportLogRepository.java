package com.firesafety.repository;

import com.firesafety.entity.ImportLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {
}
