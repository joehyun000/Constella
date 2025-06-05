package com.core.constella.api.diary.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.core.constella.api.diary.service.DiaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final DiaryService diaryService;

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        DiaryService.StatsSummary summary = diaryService.getStatsSummary();
        Map<String, Object> result = new HashMap<>();
        result.put("totalDiaries", summary.totalDiaries);
        result.put("totalCountries", summary.totalCountries);
        result.put("mostVisitedCountry", summary.mostVisitedCountry);
        return result;
    }

    @GetMapping("/by-country")
    public List<Map<String, Object>> getByCountry() {
        List<DiaryService.StatsByCountry> stats = diaryService.getStatsByCountry();
        List<Map<String, Object>> result = new ArrayList<>();
        for (DiaryService.StatsByCountry s : stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("countryName", s.countryName);
            map.put("count", s.count);
            result.add(map);
        }
        return result;
    }
} 