package com.core.constella.api.diary.repository;

import com.core.constella.api.diary.domain.ConstellationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstellationPointRepository extends JpaRepository<ConstellationPoint, Long> {
    List<ConstellationPoint> findByConstellationIdOrderByOrderInConstellation(Long constellationId);
}
