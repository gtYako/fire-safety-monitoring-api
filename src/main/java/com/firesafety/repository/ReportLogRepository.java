package com.firesafety.repository;

import com.firesafety.entity.ReportLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {
}
