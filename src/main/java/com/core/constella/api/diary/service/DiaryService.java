package com.core.constella.api.diary.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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
import lombok.Getter;
import lombok.Builder;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final CountryService countryService;

    // 이미지 저장 경로를 외부 디렉토리로 설정
    @Value("${app.upload.dir:./uploads}") // application.properties에서 설정 가능, 기본값은 현재 디렉토리의 uploads 폴더
    private String uploadDir;

    @Transactional
    public void createEntry(DiaryCreateRequest request) throws IOException {
        try {
            System.out.println("Starting diary creation process...");
            System.out.println("Location Code: " + request.getLocationCode());
            System.out.println("Title: " + request.getTitle());
            System.out.println("Date: " + request.getDate());
            System.out.println("Number of images: " + (request.getImages() != null ? request.getImages().size() : 0));

            Diary diary = diaryRepository.findByLocationCode(request.getLocationCode())
                    .orElse(Diary.builder()
                            .locationCode(request.getLocationCode())
                            .latitude(request.getLatitude() != null ? request.getLatitude() : 0.0)
                            .longitude(request.getLongitude() != null ? request.getLongitude() : 0.0)
                            .build());

            diary = diaryRepository.save(diary);

            DiaryEntry entry = DiaryEntry.builder()
                    .title(request.getTitle())
                    .contents(request.getContents())
                    .date(request.getDate())
                    .diary(diary)
                    .build();

            // 이미지 업로드 및 저장 로직 다시 포함
            if (request.getImages() != null) {
                 Path uploadPath = Paths.get(uploadDir);
                 Files.createDirectories(uploadPath);
                 for (MultipartFile file : request.getImages()) {
                     if (!file.isEmpty()) {
                         try {
                             String originalFilename = file.getOriginalFilename();
                             System.out.println("Processing image: " + originalFilename);
                             if (originalFilename == null || !isValidImageFile(originalFilename)) {
                                 throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다: " + originalFilename);
                             }
                             String fileName = UUID.randomUUID() + "_" + originalFilename;
                             Path imagePath = uploadPath.resolve(fileName);
                             System.out.println("Saving image to: " + imagePath.toAbsolutePath());
                             Files.write(imagePath, file.getBytes());
                             System.out.println("Image saved successfully");
                             DiaryImage image = new DiaryImage();
                             image.setImageUrl("/images/" + fileName);
                             image.setEntry(entry);
                             entry.getImages().add(image);
                         } catch (Exception e) {
                             System.err.println("Error processing image: " + e.getMessage());
                             e.printStackTrace();
                             throw new IOException("이미지 처리 중 오류 발생: " + e.getMessage());
                         }
                     }
                 }
             }

            diary.getEntries().add(entry);
            diaryRepository.save(diary);
            System.out.println("Diary entry created successfully");
        } catch (Exception e) {
            System.err.println("Error in createEntry: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 이미지 파일 확장자 검증 메서드 다시 포함
    private boolean isValidImageFile(String filename) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        String lowercaseFilename = filename.toLowerCase();
        for (String ext : allowedExtensions) {
            if (lowercaseFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    // locationCode에 해당하는 다이어리 글 목록과 각 글의 내용을 병합하여 반환
    public List<DiaryMergedResponse> getMergedEntries(String locationCode) {
        List<DiaryMergedResponse> responseList = new ArrayList<>();
        // 해당 locationCode를 가진 Diary만 찾아서 처리
        diaryRepository.findByLocationCode(locationCode).ifPresent(diary -> {
            for (DiaryEntry entry : diary.getEntries()) {
                StringBuilder contentBuilder = new StringBuilder();
                // contents 리스트의 각 문자열을 병합
                for (String content : entry.getContents()) {
                    contentBuilder.append(content).append("\n");
                }

                List<String> images = new ArrayList<>();
                // 이미지 URL 목록 추출
                for (DiaryImage img : entry.getImages()) {
                    images.add(img.getImageUrl());
                }

                // 병합된 응답 객체 생성
                DiaryMergedResponse response = DiaryMergedResponse.builder()
                        .id(entry.getId())
                        .locationCode(diary.getLocationCode())
                        .mergedTitle(entry.getTitle())
                        .mergedContent(contentBuilder.toString())
                        .imageUrls(images)
                        .date(entry.getDate())
                        .build();

                responseList.add(response);
            }
        });

        // 시간순 정렬 (예: 최신순)
        responseList.sort((a, b) -> b.getId().compareTo(a.getId())); // id 기준, 필요시 date로 변경

        return responseList;
    }

    // 모든 locationCode의 모든 카드(일기) 리스트를 시간순으로 반환하는 메서드 추가
    public List<DiaryMergedResponse> getAllMergedEntries() {
        List<DiaryMergedResponse> responseList = new ArrayList<>();
        List<Diary> diaries = diaryRepository.findAll();

        for (Diary diary : diaries) {
            for (DiaryEntry entry : diary.getEntries()) {
                StringBuilder contentBuilder = new StringBuilder();
                for (String content : entry.getContents()) {
                    contentBuilder.append(content).append("\n");
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
                        .date(entry.getDate())
                        .build();

                responseList.add(response);
            }
        }

        // 시간순 정렬 (예: 최신순)
        responseList.sort((a, b) -> b.getId().compareTo(a.getId()));

        return responseList;
    }

    // --- 통계용 메서드 추가 ---
    @Getter
    @Builder
    public static class StatsSummary {
        private long totalDiaries;
        private long totalCountries;
        private String mostVisitedCountry;
    }

    public StatsSummary getStatsSummary() {
        // 통계 계산 로직 임시 구현 사용
        long totalDiaries = diaryRepository.count(); // 전체 일기 수
        long totalCountries = diaryRepository.findAll().stream().map(Diary::getLocationCode).distinct().count(); // 임시 구현

        // 가장 많이 방문한 나라 계산 임시 구현
        String mostVisitedCountry = "N/A"; // 임시값
        // 추가 구현 필요

        return StatsSummary.builder()
                .totalDiaries(totalDiaries)
                .totalCountries(totalCountries)
                .mostVisitedCountry(mostVisitedCountry)
                .build();
    }

    @Getter
    @Builder
    public static class StatsByCountry {
        private String countryName;
        private long count;
    }

    public List<StatsByCountry> getStatsByCountry() {
        // locationCode별 일기 개수 집계 임시 구현
        Map<String, Long> countsMap = diaryRepository.findAll().stream()
                                       .collect(Collectors.groupingBy(Diary::getLocationCode, Collectors.counting()));

        List<StatsByCountry> statsList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countsMap.entrySet()) {
            String locationCode = entry.getKey();
            long count = entry.getValue();
            String countryName = CountryService.getCountryNameKoByCode().getOrDefault(locationCode, locationCode); // 국가 코드 -> 한글 이름 변환
            statsList.add(StatsByCountry.builder().countryName(countryName).count(count).build());
        }

        // 개수 기준 내림차순 정렬
        statsList.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));

        return statsList;
    }
}
