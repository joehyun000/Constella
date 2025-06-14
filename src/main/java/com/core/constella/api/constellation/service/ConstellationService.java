package com.core.constella.api.constellation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.core.constella.api.constellation.domain.Constellation;
import com.core.constella.api.constellation.domain.ConstellationHistory;
import com.core.constella.api.constellation.dto.ConstellationHistoryResponse;
import com.core.constella.api.constellation.dto.ConstellationPinDto;
import com.core.constella.api.constellation.dto.ConstellationResponse;
import com.core.constella.api.constellation.dto.ConstellationSaveRequest;
import com.core.constella.api.constellation.repository.ConstellationHistoryRepository;
import com.core.constella.api.constellation.repository.ConstellationRepository;
import com.core.constella.api.country.service.CountryService;
import com.core.constella.api.diary.domain.Diary;
import com.core.constella.api.diary.repository.DiaryRepository;
import com.core.constella.api.user.domain.User;
import com.core.constella.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConstellationService {
    private final DiaryRepository diaryRepository;
    private final ConstellationRepository constellationRepository;
    private final UserRepository userRepository;
    private final ConstellationHistoryRepository constellationHistoryRepository;
    private static final Logger log = LoggerFactory.getLogger(ConstellationService.class);

    /**
     * 특정 userId의 핀(나라) 목록을 위도/경도/이름과 함께 최단경로 순서로 반환
     */
    public List<ConstellationPinDto> getConstellationPinsForUser(Long userId) {
        log.info("Getting constellation pins for user: {}", userId);
        List<Diary> diaries = diaryRepository.findByUser_Id(userId);
        log.info("Found {} diaries for user {}", diaries.size(), userId);
        List<ConstellationPinDto> pins = new ArrayList<>();
        
        for (Diary d : diaries) {
            String code = d.getLocationCode();
            String nameKo = CountryService.getCountryNameKoByCode().getOrDefault(code, code);
            Double lat = d.getLatitude();
            Double lng = d.getLongitude();
            
            // 위도/경도가 null이거나 0인 경우 countries.json에서 가져오기
            if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
                CountryService.CountryInfo info = CountryService.COUNTRY_INFO_MAP.get(nameKo);
                if (info != null) {
                    lat = info.lat;
                    lng = info.lng;
                    log.info("Using coordinates from CountryService for {}: lat={}, lng={}", code, lat, lng);
                } else {
                    log.warn("No coordinates found for location code: {}", code);
                    continue;
                }
            }
            
            log.info("Processing diary: code={}, nameKo={}, lat={}, lng={}", code, nameKo, lat, lng);
            pins.add(ConstellationPinDto.builder()
                    .locationCode(code)
                    .nameKo(nameKo)
                    .lat(lat)
                    .lng(lng)
                    .build());
        }
        
        // Nearest Neighbor로 한 줄로 정렬
        List<ConstellationPinDto> sortedPins = sortPinsByNearestNeighbor(pins);
        log.info("Returning {} sorted pins", sortedPins.size());
        return sortedPins;
    }

    // Nearest Neighbor 정렬 (핀 개수 적을 때 충분)
    private List<ConstellationPinDto> sortPinsByNearestNeighbor(List<ConstellationPinDto> pins) {
        if (pins.size() <= 2) return pins;
        List<ConstellationPinDto> result = new ArrayList<>();
        List<ConstellationPinDto> remain = new ArrayList<>(pins);
        result.add(remain.remove(0));
        while (!remain.isEmpty()) {
            ConstellationPinDto curr = result.get(result.size() - 1);
            ConstellationPinDto next = remain.stream()
                .min((a, b) -> Double.compare(distance(curr, a), distance(curr, b)))
                .orElse(remain.get(0));
            result.add(next);
            remain.remove(next);
        }
        return result;
    }
    private double distance(ConstellationPinDto a, ConstellationPinDto b) {
        double dx = a.getLat() - b.getLat();
        double dy = a.getLng() - b.getLng();
        return dx * dx + dy * dy;
    }

    // 별자리 저장 시 히스토리도 함께 저장
    @Transactional
    public void saveConstellationHistory(Constellation constellation) {
        try {
            log.info("Starting to save constellation history for user {} and constellation {}", 
                constellation.getUser().getId(), constellation.getId());
            log.info("Pin order: {}", constellation.getPinOrder());
            
            // Validate constellation data
            if (constellation.getUser() == null) {
                log.error("Constellation user is null");
                throw new IllegalArgumentException("Constellation user cannot be null");
            }
            if (constellation.getPinOrder() == null || constellation.getPinOrder().isEmpty()) {
                log.error("Constellation pin order is null or empty");
                throw new IllegalArgumentException("Constellation pin order cannot be null or empty");
            }
            
            ConstellationHistory history = ConstellationHistory.builder()
                    .user(constellation.getUser())
                    .constellation(constellation)
                    .pinOrder(constellation.getPinOrder())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("Created history object: {}", history);
            log.info("History object details - userId: {}, constellationId: {}, pinOrder size: {}", 
                history.getUser().getId(), 
                history.getConstellation().getId(), 
                history.getPinOrder().size());
            
            log.info("Attempting to save ConstellationHistory for user: {} and constellation: {}", 
                constellation.getUser().getId(), constellation.getId());
            
            history = constellationHistoryRepository.save(history);
            
            log.info("Successfully saved constellation history: id={}, createdAt={}", 
                history.getId(), history.getCreatedAt());
        } catch (Exception e) {
            log.error("Failed to save constellation history: {}", e.getMessage(), e);
            log.error("Stack trace: ", e);
            throw new RuntimeException("Failed to save constellation history", e);
        }
    }

    // 별자리 히스토리 조회 (페이지네이션)
    public Page<ConstellationHistoryResponse> getConstellationHistory(Long userId, Pageable pageable) {
        log.info("Getting paginated constellation history for user {}: page={}, size={}", 
            userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<ConstellationHistory> histories = constellationHistoryRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        log.info("Found {} history records for user {}", histories.getTotalElements(), userId);
        return histories.map(this::convertToHistoryResponse);
    }

    // 별자리 히스토리 조회 (전체)
    public List<ConstellationHistoryResponse> getAllConstellationHistory(Long userId) {
        log.info("Getting all constellation history for user {}", userId);
        List<ConstellationHistory> histories = constellationHistoryRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        log.info("Found {} total history records for user {}", histories.size(), userId);
        return histories.stream()
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());
    }

    private ConstellationHistoryResponse convertToHistoryResponse(ConstellationHistory history) {
        log.debug("Converting history to response: id={}, userId={}", history.getId(), history.getUser().getId());
        List<ConstellationPinDto> pins = new ArrayList<>();
        
        for (String countryName : history.getPinOrder()) {
            CountryService.CountryInfo info = CountryService.COUNTRY_INFO_MAP.get(countryName);
            if (info == null) {
                log.warn("No country info found for: {}", countryName);
                continue;
            }
            
            String code = info.code;
            Double lat = info.lat;
            Double lng = info.lng;
            
            // Try to get coordinates from diary first
            Diary diary = diaryRepository.findByUser_IdAndLocationCode(history.getUser().getId(), code).orElse(null);
            if (diary != null && diary.getLatitude() != null && diary.getLongitude() != null
                && diary.getLatitude() != 0.0 && diary.getLongitude() != 0.0) {
                lat = diary.getLatitude();
                lng = diary.getLongitude();
                log.debug("Using coordinates from diary for {}: lat={}, lng={}", code, lat, lng);
            } else {
                log.debug("Using coordinates from CountryService for {}: lat={}, lng={}", code, lat, lng);
            }
            
            pins.add(ConstellationPinDto.builder()
                    .locationCode(code)
                    .nameKo(countryName)
                    .lat(lat)
                    .lng(lng)
                    .build());
        }
        
        ConstellationHistoryResponse response = ConstellationHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .constellationId(history.getConstellation().getId())
                .pinOrder(history.getPinOrder())
                .createdAt(history.getCreatedAt())
                .pins(pins)
                .build();
        log.debug("Converted history to response: id={}, pins={}", response.getId(), response.getPins().size());
        return response;
    }

    // 저장된 별자리(커스텀 순서) 핀 정보를 좌표, 한글이름, 코드와 함께 반환
    public List<ConstellationPinDto> getSavedConstellationPinsForUser(Long userId) {
        log.info("Getting saved constellation pins for user: {}", userId);
        Constellation constellation = constellationRepository.findByUser_Id(userId)
            .orElseThrow(() -> new IllegalArgumentException("Constellation not found for userId: " + userId));
        List<String> pinOrder = constellation.getPinOrder();
        log.info("Found pin order: {}", pinOrder);
        
        List<ConstellationPinDto> pins = new ArrayList<>();
        
        for (String code : pinOrder) {
            if (code == null || code.trim().isEmpty()) {
                log.warn("Skipping null or empty code");
                continue;
            }
            
            log.info("Processing pin with code: {}", code);
            
            // Get country name in Korean
            String nameKo = CountryService.getCountryNameKoByCode().getOrDefault(code, code);
            if (nameKo == null || nameKo.trim().isEmpty()) {
                log.warn("No Korean name found for code: {}", code);
                continue;
            }
            log.info("Found name in Korean: {} for code: {}", nameKo, code);
            
            // Get coordinates from diary first
            Diary diary = diaryRepository.findByUser_IdAndLocationCode(userId, code).orElse(null);
            Double lat = null;
            Double lng = null;
            
            if (diary != null && diary.getLatitude() != null && diary.getLongitude() != null
                && diary.getLatitude() != 0.0 && diary.getLongitude() != 0.0) {
                lat = diary.getLatitude();
                lng = diary.getLongitude();
                log.info("Using coordinates from diary: lat={}, lng={}", lat, lng);
            } else {
                // If no diary coordinates, try to get from CountryService
                CountryService.CountryInfo info = CountryService.COUNTRY_INFO_MAP.get(nameKo);
                if (info != null) {
                    lat = info.lat;
                    lng = info.lng;
                    log.info("Using coordinates from CountryService: lat={}, lng={}", lat, lng);
                } else {
                    log.warn("No coordinates found for code: {}", code);
                    continue;
                }
            }
            
            // Validate coordinates
            if (lat == null || lng == null || 
                Double.isNaN(lat) || Double.isNaN(lng) ||
                Double.isInfinite(lat) || Double.isInfinite(lng)) {
                log.warn("Invalid coordinates for code: {}", code);
                continue;
            }
            
            try {
                // Ensure all fields are properly formatted
                String trimmedCode = code.trim();
                String trimmedNameKo = nameKo.trim();
                
                // Validate string fields
                if (trimmedCode.isEmpty() || trimmedNameKo.isEmpty()) {
                    log.warn("Empty string fields for code: {}", code);
                    continue;
                }
                
                // Create pin with validated data
                ConstellationPinDto pin = ConstellationPinDto.builder()
                    .locationCode(trimmedCode)
                    .nameKo(trimmedNameKo)
                    .lat(lat)
                    .lng(lng)
                    .build();
                
                // Validate the created pin
                if (pin.getLocationCode() == null || pin.getNameKo() == null ||
                    pin.getLat() == null || pin.getLng() == null) {
                    log.warn("Created pin has null fields: {}", pin);
                    continue;
                }
                
                log.info("Created valid pin: {}", pin);
                pins.add(pin);
            } catch (Exception e) {
                log.error("Error creating pin for code {}: {}", code, e.getMessage());
                continue;
            }
        }
        
        log.info("Returning {} valid pins", pins.size());
        return pins;
    }

    // 별자리 저장
    @Transactional
    public ConstellationResponse saveConstellation(ConstellationSaveRequest req) {
        log.info("Saving constellation for user {}", req.getUserId());
        
        // Validate request
        if (req.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getUserId()));
        log.debug("Found user: id={}, username={}", user.getId(), user.getUsername());
        
        // Get all diaries for the user
        List<Diary> diaries = diaryRepository.findByUser_Id(req.getUserId());
        if (diaries.isEmpty()) {
            throw new IllegalArgumentException("No diaries found for user: " + req.getUserId());
        }
        
        // Extract location codes from diaries
        List<String> pinOrder = diaries.stream()
            .map(Diary::getLocationCode)
            .distinct()
            .collect(Collectors.toList());
            
        log.info("Extracted pin order from diaries: {}", pinOrder);
        
        // Try to find existing constellation for the user
        Constellation constellation = constellationRepository.findByUser_Id(req.getUserId())
                .orElse(null);
                
        if (constellation == null) {
            log.info("Creating new constellation for user {}", req.getUserId());
            // Create new constellation if none exists
            constellation = Constellation.builder()
                    .user(user)
                    .pinOrder(pinOrder)
                    .name("My Constellation")  // 기본 이름 설정
                    .build();
        } else {
            log.info("Updating existing constellation for user {}: id={}", req.getUserId(), constellation.getId());
            // Update existing constellation
            constellation.setPinOrder(pinOrder);
            constellation.setName("My Constellation");  // 기본 이름 설정
        }
        
        try {
            // Save constellation first
            constellation = constellationRepository.save(constellation);
            log.info("Successfully saved constellation: id={}", constellation.getId());
            
            // Save constellation history
            try {
                saveConstellationHistory(constellation);
            } catch (Exception e) {
                log.error("Failed to save constellation history, rolling back constellation save: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save constellation history", e);
            }

            // Create response with validated data
            ConstellationResponse response = ConstellationResponse.builder()
                    .id(constellation.getId())
                    .userId(user.getId())
                    .pinOrder(constellation.getPinOrder())
                    .name(constellation.getName())
                    .build();
            
            // Validate response
            if (response.getId() == null || response.getUserId() == null ||
                response.getPinOrder() == null || response.getName() == null) {
                throw new IllegalStateException("Created response has null fields");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Failed to save constellation for user {}: {}", req.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save constellation", e);
        }
    }

    // 별자리 조회 (userId별)
    public ConstellationResponse getConstellationByUserId(Long userId) {
        Constellation constellation = constellationRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Constellation not found for userId: " + userId));
        return ConstellationResponse.builder()
                .id(constellation.getId())
                .userId(userId)
                .pinOrder(constellation.getPinOrder())
                .name(constellation.getName())
                .build();
    }

    // 중복 별자리 정리 (가장 최근 것 제외하고 삭제)
    @Transactional
    public void cleanupDuplicateConstellations(Long userId) {
        List<Constellation> constellations = constellationRepository.findAllByUser_Id(userId);
        if (constellations.size() <= 1) {
            return;
        }
        
        // Sort by ID in descending order (assuming higher ID = more recent)
        constellations.sort((a, b) -> b.getId().compareTo(a.getId()));
        
        // Keep the first one (most recent) and delete the rest
        List<Constellation> toDelete = constellations.subList(1, constellations.size());
        for (Constellation c : toDelete) {
            constellationRepository.delete(c);
        }
        
        log.info("Cleaned up {} duplicate constellations for user {}", toDelete.size(), userId);
    }
} 