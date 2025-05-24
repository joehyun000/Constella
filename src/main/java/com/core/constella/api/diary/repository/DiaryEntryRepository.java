package com.core.constella.api.diary.repository;

import com.core.constella.api.diary.domain.DiaryEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    //contents와 images까지 한번에 보내기
    @EntityGraph(attributePaths = {"contents", "images"})
    Optional<DiaryEntry> findById(Long id);
}
