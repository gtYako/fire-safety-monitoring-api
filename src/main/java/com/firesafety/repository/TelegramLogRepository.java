package com.firesafety.repository;

import com.firesafety.entity.TelegramLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramLogRepository extends JpaRepository<TelegramLog, Long> {
    Page<TelegramLog> findAll(Pageable pageable);
}
