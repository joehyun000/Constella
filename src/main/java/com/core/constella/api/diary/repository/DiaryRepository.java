package com.core.constella.api.diary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.core.constella.api.diary.domain.Diary;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByLocationCode(String locationCode);

    // 나라(locationCode)별 일기 개수
    @Query("SELECT d.locationCode, COUNT(e) FROM Diary d JOIN d.entries e GROUP BY d.locationCode")
    List<Object[]> countEntriesByCountryGroup();

    @Query("SELECT d.locationCode, COUNT(e) as cnt FROM Diary d JOIN d.entries e GROUP BY d.locationCode ORDER BY cnt DESC")
    Object[] findMostVisitedCountryNative();

    // 가장 많이 등장한 나라(최다 일기) - Pageable로 1개만 가져오기
    @Query("SELECT d.locationCode, COUNT(e) as cnt FROM Diary d JOIN d.entries e GROUP BY d.locationCode ORDER BY cnt DESC")
    List<Object[]> findMostVisitedCountries(Pageable pageable);

}
