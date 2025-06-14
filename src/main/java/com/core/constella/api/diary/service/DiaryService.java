package com.core.constella.api.diary.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import com.core.constella.api.user.domain.User;
import com.core.constella.api.user.service.UserService;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final CountryService countryService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(DiaryService.class);

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

            if (request.getUserId() == null) {
                throw new IllegalArgumentException("userId는 필수입니다.");
            }
            User user = userService.findById(request.getUserId());
            if (user == null) {
                throw new IllegalArgumentException("해당 userId의 유저가 존재하지 않습니다: " + request.getUserId());
            }

            System.out.println("User found: " + user.getId() + ", " + user.getUsername());

            // userId와 locationCode로 Diary 찾기 (user별로 Diary 분리)
            Double lat = request.getLatitude();
            Double lng = request.getLongitude();
            if (lat == null || lat == 0.0 || lng == null || lng == 0.0) {
                String nameKo = CountryService.getCountryNameKoByCode().get(request.getLocationCode());
                CountryService.CountryInfo info = null;
                if (nameKo != null) {
                    info = CountryService.COUNTRY_INFO_MAP.get(nameKo);
                }
                if (info != null) {
                    lat = info.lat;
                    lng = info.lng;
                }
            }

            // Find or create diary with proper user association
            Optional<Diary> existingDiary = diaryRepository.findByUser_IdAndLocationCode(request.getUserId(), request.getLocationCode());
            Diary diary;
            if (existingDiary.isPresent()) {
                diary = existingDiary.get();
                log.info("Found existing diary for user {} and location {}", request.getUserId(), request.getLocationCode());
            } else {
                log.info("Creating new diary for user {} and location {}", request.getUserId(), request.getLocationCode());
                diary = Diary.builder()
                        .locationCode(request.getLocationCode())
                        .latitude(lat)
                        .longitude(lng)
                        .user(user)
                        .build();
            }
            
            // Always ensure user is set
            if (diary.getUser() == null || !diary.getUser().getId().equals(request.getUserId())) {
                log.info("Setting user {} for diary", request.getUserId());
                diary.setUser(user);
            }

            // 위도/경도 정보 강제 업데이트
            String nameKo = CountryService.getCountryNameKoByCode().get(request.getLocationCode());
            if (nameKo != null) {
                CountryService.CountryInfo info = CountryService.COUNTRY_INFO_MAP.get(nameKo);
                if (info != null) {
                    diary.setLatitude(info.lat);
                    diary.setLongitude(info.lng);
                    log.info("Updated coordinates for {}: lat={}, lng={}", nameKo, info.lat, info.lng);
                }
            }

            System.out.println("Diary userId: " + (diary.getUser() != null ? diary.getUser().getId() : "null"));

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

    // userId별 카드 리스트 반환
    public List<DiaryMergedResponse> getAllMergedEntriesByUserId(Long userId) {
        List<Diary> diaries = diaryRepository.findByUser_Id(userId);
        List<DiaryMergedResponse> allResponses = new ArrayList<>();
        for (Diary diary : diaries) {
            for (DiaryEntry entry : diary.getEntries()) {
                StringBuilder contentBuilder = new StringBuilder();
                for (String content : entry.getContents()) {
                    contentBuilder.append(content).append("\n");
                }

                List<String> images = entry.getImages().stream()
                    .map(DiaryImage::getImageUrl)
                    .collect(Collectors.toList());

                allResponses.add(DiaryMergedResponse.builder()
                    .id(entry.getId())
                    .locationCode(diary.getLocationCode())
                    .mergedTitle(entry.getTitle())
                    .mergedContent(contentBuilder.toString())
                    .imageUrls(images)
                    .date(entry.getDate())
                    .build());
            }
        }
        // 시간순 정렬 (예: 최신순)
        allResponses.sort((a, b) -> b.getId().compareTo(a.getId()));
        return allResponses;
    }

    // userId와 locationCode로 병합된 카드 리스트 반환
    public List<DiaryMergedResponse> getMergedEntriesByUserIdAndLocationCode(Long userId, String locationCode) {
        Optional<Diary> diaryOpt = diaryRepository.findByUser_IdAndLocationCode(userId, locationCode);
        if (diaryOpt.isEmpty()) {
            return List.of();
        }

        Diary diary = diaryOpt.get();
        List<DiaryMergedResponse> responses = new ArrayList<>();
        for (DiaryEntry entry : diary.getEntries()) {
            StringBuilder contentBuilder = new StringBuilder();
            for (String content : entry.getContents()) {
                contentBuilder.append(content).append("\n");
            }

            List<String> images = entry.getImages().stream()
                .map(DiaryImage::getImageUrl)
                .collect(Collectors.toList());

            responses.add(DiaryMergedResponse.builder()
                .id(entry.getId())
                .locationCode(diary.getLocationCode())
                .mergedTitle(entry.getTitle())
                .mergedContent(contentBuilder.toString())
                .imageUrls(images)
                .date(entry.getDate())
                .build());
        }
        // 시간순 정렬 (예: 최신순)
        responses.sort((a, b) -> b.getId().compareTo(a.getId()));
        return responses;
    }

    // --- 통계용 메서드 추가 ---
    @Getter
    @Builder
    public static class StatsSummary {
        private long totalDiaries;
        private long totalCountries;
        private String mostVisitedCountry;
    }

    // userId별 통계 반환
    public StatsSummary getStatsSummaryByUserId(Long userId) {
        log.info("Attempting to get stats summary for userId: {}", userId);
        List<Diary> diaries = diaryRepository.findByUser_Id(userId);
        log.info("Found {} diaries for userId: {}", diaries.size(), userId);
        long totalDiaries = diaries.stream().mapToLong(d -> d.getEntries().size()).sum();
        long totalCountries = diaries.stream().map(Diary::getLocationCode).distinct().count();

        Map<String, Long> countryCount = new HashMap<>();
        for (Diary d : diaries) {
            countryCount.put(d.getLocationCode(),
                countryCount.getOrDefault(d.getLocationCode(), 0L) + d.getEntries().size());
        }
        String mostVisitedCountry = countryCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        mostVisitedCountry = CountryService.getCountryNameKoByCode().getOrDefault(mostVisitedCountry, mostVisitedCountry);

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

    // userId별 나라별 통계 반환
    public List<StatsByCountry> getStatsByCountryByUserId(Long userId) {
        List<Diary> diaries = diaryRepository.findByUser_Id(userId);
        Map<String, Long> countsMap = diaries.stream()
            .collect(Collectors.groupingBy(Diary::getLocationCode,
                Collectors.summingLong(d -> d.getEntries().size())));
        List<StatsByCountry> statsList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countsMap.entrySet()) {
            String locationCode = entry.getKey();
            long count = entry.getValue();
            String countryName = CountryService.getCountryNameKoByCode().getOrDefault(locationCode, locationCode);
            statsList.add(StatsByCountry.builder().countryName(countryName).count(count).build());
        }
        statsList.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return statsList;
    }

    public static class PinInfo {
        public String locationCode;
        public String nameKo;
        public Double lat;
        public Double lng;
        public PinInfo(String locationCode, String nameKo, Double lat, Double lng) {
            this.locationCode = locationCode;
            this.nameKo = nameKo;
            this.lat = lat;
            this.lng = lng;
        }
    }

    /**
     * 특정 userId의 핀(나라) 목록을 위도/경도/이름과 함께 최단경로 순서로 반환
     */
    public List<PinInfo> getConstellationPinsForUser(Long userId) {
        List<Diary> diaries = diaryRepository.findByUser_Id(userId);
        List<PinInfo> pins = new ArrayList<>();
        for (Diary d : diaries) {
            String code = d.getLocationCode();
            String nameKo = CountryService.getCountryNameKoByCode().getOrDefault(code, code);
            Double lat = d.getLatitude();
            Double lng = d.getLongitude();
            pins.add(new PinInfo(code, nameKo, lat, lng));
        }
        // Nearest Neighbor로 한 줄로 정렬
        return sortPinsByNearestNeighbor(pins);
    }

    // Nearest Neighbor 정렬 (핀 개수 적을 때 충분)
    private List<PinInfo> sortPinsByNearestNeighbor(List<PinInfo> pins) {
        if (pins.size() <= 2) return pins;
        List<PinInfo> result = new ArrayList<>();
        List<PinInfo> remain = new ArrayList<>(pins);
        result.add(remain.remove(0));
        while (!remain.isEmpty()) {
            PinInfo curr = result.get(result.size() - 1);
            PinInfo next = remain.stream()
                .min((a, b) -> Double.compare(distance(curr, a), distance(curr, b)))
                .orElse(remain.get(0));
            result.add(next);
            remain.remove(next);
        }
        return result;
    }
    private double distance(PinInfo a, PinInfo b) {
        double dx = a.lat - b.lat;
        double dy = a.lng - b.lng;
        return dx * dx + dy * dy;
    }

    public List<Diary> getDiariesByUserId(Long userId) {
        return diaryRepository.findByUser_Id(userId);
    }

    // --- 추가: 이미 저장된 Diary의 위도/경도를 countries.json 기반으로 일괄 업데이트하는 메서드 ---
    @Transactional
    public void updateAllDiariesWithCountryLatLng() {
        List<Diary> diaries = diaryRepository.findAll();
        for (Diary diary : diaries) {
            if (diary.getLatitude() == null || diary.getLatitude() == 0.0 ||
                diary.getLongitude() == null || diary.getLongitude() == 0.0) {
                String nameKo = CountryService.getCountryNameKoByCode().get(diary.getLocationCode());
                CountryService.CountryInfo info = null;
                if (nameKo != null) {
                    info = CountryService.COUNTRY_INFO_MAP.get(nameKo);
                }
                if (info != null) {
                    diary.setLatitude(info.lat);
                    diary.setLongitude(info.lng);
                }
            }
        }
        diaryRepository.saveAll(diaries);
    }

    private DiaryMergedResponse convertToMergedResponse(Diary diary) {
        List<DiaryMergedResponse> responses = new ArrayList<>();
        for (DiaryEntry entry : diary.getEntries()) {
            StringBuilder contentBuilder = new StringBuilder();
            for (String content : entry.getContents()) {
                contentBuilder.append(content).append("\n");
            }

            List<String> images = entry.getImages().stream()
                .map(DiaryImage::getImageUrl)
                .collect(Collectors.toList());

            responses.add(DiaryMergedResponse.builder()
                .id(entry.getId())
                .locationCode(diary.getLocationCode())
                .mergedTitle(entry.getTitle())
                .mergedContent(contentBuilder.toString())
                .imageUrls(images)
                .date(entry.getDate())
                .build());
        }
        // 시간순 정렬 (예: 최신순)
        responses.sort((a, b) -> b.getId().compareTo(a.getId()));
        return responses.isEmpty() ? null : responses.get(0);
    }
}
