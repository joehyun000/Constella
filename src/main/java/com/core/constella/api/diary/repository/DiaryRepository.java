package com.core.constella.api.diary.repository;

import com.core.constella.api.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByLocationCode(String locationCode);
}
