package com.core.constella.api.constellation.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.core.constella.api.constellation.domain.ConstellationHistory;

public interface ConstellationHistoryRepository extends JpaRepository<ConstellationHistory, Long> {
    Page<ConstellationHistory> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<ConstellationHistory> findByUser_IdOrderByCreatedAtDesc(Long userId);
} 