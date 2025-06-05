package com.core.constella.api.diary.repository;

import com.core.constella.api.diary.domain.Constellation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstellationRepository extends JpaRepository<Constellation, Long> {

}
