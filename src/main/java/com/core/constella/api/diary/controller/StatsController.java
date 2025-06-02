package com.core.constella.api.diary.controller;

import com.core.constella.api.diary.domain.Diary;
import com.core.constella.api.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final DiaryRepository diaryRepository;

    @GetMapping("/summary")
    public Map<String, Object> getSummaryStats() {
        List<Diary> all = diaryRepository.findAll();

        int totalDiaries = 0;
        Map<String, Integer> countryCount = new HashMap<>();

        for (Diary diary : all) {
            int entries = diary.getEntries().size();
            totalDiaries += entries;
            countryCount.put(diary.getLocationCode(), entries);
        }

        String mostVisited = countryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        Map<String, Object> result = new HashMap<>();
        result.put("totalDiaries", totalDiaries);
        result.put("totalCountries", countryCount.size());
        result.put("mostVisitedCountry", mostVisited);

        return result;
    }

    @GetMapping("/by-country")
    public List<Map<String, Object>> getStatsByCountry() {
        List<Diary> all = diaryRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Diary diary : all) {
            Map<String, Object> map = new HashMap<>();
            map.put("countryName", diary.getLocationCode()); // 필요한 경우 나라 이름 변환
            map.put("count", diary.getEntries().size());
            result.add(map);
        }

        return result;
    }
}

