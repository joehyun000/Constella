package com.core.constella.api.constellation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.core.constella.api.constellation.domain.Constellation;

public interface ConstellationRepository extends JpaRepository<Constellation, Long> {
    Optional<Constellation> findByUser_Id(Long userId);
    List<Constellation> findAllByUser_Id(Long userId);
} 