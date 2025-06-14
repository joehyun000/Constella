package com.core.constella.api.diary.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.core.constella.api.diary.service.DiaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final DiaryService diaryService;
    private static final Logger log = LoggerFactory.getLogger(StatsController.class);

    @GetMapping("/summary/{userId}")
    public Map<String, Object> getSummaryByUser(@PathVariable Long userId) {
        log.info("StatsController: Received request for summary for userId: {}", userId);
        DiaryService.StatsSummary summary = diaryService.getStatsSummaryByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("totalDiaries", summary.getTotalDiaries());
        result.put("totalCountries", summary.getTotalCountries());
        result.put("mostVisitedCountry", summary.getMostVisitedCountry());
        return result;
    }

    @GetMapping("/by-country/{userId}")
    public List<Map<String, Object>> getByCountryByUser(@PathVariable Long userId) {
        log.info("StatsController: Received request for stats by country for userId: {}", userId);
        List<DiaryService.StatsByCountry> stats = diaryService.getStatsByCountryByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (DiaryService.StatsByCountry s : stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("countryName", s.getCountryName());
            map.put("count", s.getCount());
            result.add(map);
        }
        return result;
    }
} 