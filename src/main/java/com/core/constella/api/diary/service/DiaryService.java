package com.core.constella.api.diary.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.core.constella.api.country.service.CountryService;
import com.core.constella.api.diary.domain.Diary;
import com.core.constella.api.diary.domain.DiaryEntry;
import com.core.constella.api.diary.domain.DiaryImage;
import com.core.constella.api.diary.dto.DiaryCreateRequest;
import com.core.constella.api.diary.dto.DiaryMergedResponse;
import com.core.constella.api.diary.repository.DiaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final CountryService countryService;

    //로컬에서 할거라 이렇게해서 이미지 가져올 예정
    private static final String UPLOAD_DIR = "C:/Users/sjun/study/constella/src/main/resources/static/images/";

    @Transactional
    public void createEntry(DiaryCreateRequest request) throws IOException {
        Diary diary = diaryRepository.findByLocationCode(request.getLocationCode())
                .orElse(Diary.builder().locationCode(request.getLocationCode()).build());

        diary = diaryRepository.save(diary); // 🔥 새로 만든 경우에는 꼭 save 해서 ID 보장

        DiaryEntry entry = DiaryEntry.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .date(request.getDate())
                .diary(diary)
                .build();

        if (request.getImages() != null) {
            for (MultipartFile file : request.getImages()) {
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path imagePath = Paths.get(UPLOAD_DIR + fileName);
                    Files.createDirectories(imagePath.getParent());
                    Files.write(imagePath, file.getBytes());

                    DiaryImage image = new DiaryImage();
                    image.setImageUrl("/images/" + fileName);
                    image.setEntry(entry);
                    entry.getImages().add(image);
                }
            }
        }

        diary.getEntries().add(entry);
        diaryRepository.save(diary); // ✅ cascade 설정 전제
    }


    public List<DiaryMergedResponse> getMergedEntries(String locationCode) {
        Diary diary = diaryRepository.findByLocationCode(locationCode).orElse(null);

        if (diary == null) {
            return new ArrayList<>(); // ✅ 예외 대신 빈 리스트 반환
        }

        List<DiaryMergedResponse> responseList = new ArrayList<>();

        for (DiaryEntry entry : diary.getEntries()) {
            StringBuilder contentBuilder = new StringBuilder();
            for (String c : entry.getContents()) {
                contentBuilder.append(c).append("\n");
            }

            List<String> images = new ArrayList<>();
            for (DiaryImage img : entry.getImages()) {
                images.add(img.getImageUrl());
            }

            DiaryMergedResponse response = DiaryMergedResponse.builder()
                    .id(entry.getId())
                    .locationCode(locationCode)
                    .mergedTitle(entry.getTitle())
                    .mergedContent(contentBuilder.toString())
                    .imageUrls(images)
                    .build();

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * 모든 locationCode의 모든 카드(일기) 리스트를 시간순으로 반환
     */
    public List<DiaryMergedResponse> getAllMergedEntries() {
        List<DiaryMergedResponse> responseList = new ArrayList<>();
        List<Diary> diaries = diaryRepository.findAll();

        for (Diary diary : diaries) {
            for (DiaryEntry entry : diary.getEntries()) {
                StringBuilder contentBuilder = new StringBuilder();
                for (String c : entry.getContents()) {
                    contentBuilder.append(c).append("\n");
                }

                List<String> images = new ArrayList<>();
                for (DiaryImage img : entry.getImages()) {
                    images.add(img.getImageUrl());
                }

                DiaryMergedResponse response = DiaryMergedResponse.builder()
                        .id(entry.getId())
                        .locationCode(diary.getLocationCode())
                        .mergedTitle(entry.getTitle())
                        .mergedContent(contentBuilder.toString())
                        .imageUrls(images)
                        .build();

                responseList.add(response);
            }
        }

        // 시간순 정렬 (예: 최신순)
        responseList.sort((a, b) -> b.getId().compareTo(a.getId())); // id 기준, 필요시 date로 변경

        return responseList;
    }

    // --- 통계용 메서드 추가 ---
    public StatsSummary getStatsSummary() {
        long totalDiaries = 0;
        int totalCountries = 0;
        String mostVisitedCountry = "없음";

        List<Object[]> countryCounts = diaryRepository.countEntriesByCountryGroup();
        totalCountries = countryCounts.size();
        for (Object[] row : countryCounts) {
            totalDiaries += ((Number) row[1]).longValue();
        }
        List<Object[]> topList = diaryRepository.findMostVisitedCountries(PageRequest.of(0, 1));
        Object[] mostVisited = topList.isEmpty() ? null : topList.get(0);
        if (mostVisited != null && mostVisited.length >= 2) {
            String locationCode = (String) mostVisited[0];
            Long count = ((Number) mostVisited[1]).longValue();
            Map<String, String> enToKo = CountryService.getCountryNameMapKoByEn();
            mostVisitedCountry = enToKo.getOrDefault(locationCode, locationCode);
        }
        return new StatsSummary(totalDiaries, totalCountries, mostVisitedCountry);
    }

    public List<StatsByCountry> getStatsByCountry() {
        List<Object[]> countryCounts = diaryRepository.countEntriesByCountryGroup();
        List<StatsByCountry> stats = new ArrayList<>();
        Map<String, String> enToKo = CountryService.getCountryNameMapKoByEn();
        for (Object[] row : countryCounts) {
            String locationCode = (String) row[0];
            long count = (Long) row[1];
            String nameKo = enToKo.getOrDefault(locationCode, locationCode);
            stats.add(new StatsByCountry(nameKo, count));
        }
        return stats;
    }

    // --- 통계 DTO ---
    public static class StatsSummary {
        public long totalDiaries;
        public int totalCountries;
        public String mostVisitedCountry;
        public StatsSummary(long totalDiaries, int totalCountries, String mostVisitedCountry) {
            this.totalDiaries = totalDiaries;
            this.totalCountries = totalCountries;
            this.mostVisitedCountry = mostVisitedCountry;
        }
    }
    public static class StatsByCountry {
        public String countryName;
        public long count;
        public StatsByCountry(String countryName, long count) {
            this.countryName = countryName;
            this.count = count;
        }
    }
}
