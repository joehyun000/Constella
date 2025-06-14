package com.core.constella.api.constellation.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.core.constella.api.constellation.dto.ConstellationHistoryResponse;
import com.core.constella.api.constellation.dto.ConstellationPinDto;
import com.core.constella.api.constellation.dto.ConstellationResponse;
import com.core.constella.api.constellation.dto.ConstellationSaveRequest;
import com.core.constella.api.constellation.service.ConstellationService;
import com.core.constella.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/constellation")
@RequiredArgsConstructor
public class ConstellationController {
    private final ConstellationService constellationService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(ConstellationController.class);

    @GetMapping("/{userId}")
    public ResponseEntity<?> getConstellationPins(@PathVariable Long userId) {
        log.info("Getting constellation pins for user: {}", userId);
        try {
            // Check if the requested user exists
            if (!userRepository.existsById(userId)) {
                log.warn("User not found: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "사용자를 찾을 수 없습니다: " + userId));
            }
            
            List<ConstellationPinDto> pins = constellationService.getConstellationPinsForUser(userId);
            log.info("Found {} pins for user {}", pins.size(), userId);
            
            if (pins.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            
            return ResponseEntity.ok(pins);
        } catch (Exception e) {
            log.error("Failed to get constellation pins for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "별자리 핀을 가져오는데 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/saved/{userId}")
    public ResponseEntity<?> getSavedConstellationPins(@PathVariable Long userId) {
        log.info("Getting saved constellation pins for user: {}", userId);
        try {
            // Check if the requested user exists
            if (!userRepository.existsById(userId)) {
                log.warn("User not found: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "사용자를 찾을 수 없습니다: " + userId));
            }
            
            List<ConstellationPinDto> pins = constellationService.getSavedConstellationPinsForUser(userId);
            log.info("Found {} saved pins for user {}", pins.size(), userId);
            
            if (pins.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            
            return ResponseEntity.ok(pins);
        } catch (IllegalArgumentException e) { // ConstellationService에서 던지는 IllegalArgumentException 처리
            log.warn("Saved constellation not found for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("message", "저장된 별자리를 찾을 수 없습니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get saved constellation pins for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "저장된 별자리 핀을 가져오는데 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveConstellation(@RequestBody ConstellationSaveRequest request) {
        log.info("Saving constellation for user: {}", request.getUserId());
        try {
            ConstellationResponse response = constellationService.saveConstellation(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) { // UserService 또는 ConstellationService에서 던지는 IllegalArgumentException 처리
            log.warn("Failed to save constellation due to invalid argument: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", "별자리 저장 실패: " + e.getMessage()));
        } catch (IllegalStateException e) { // ConstellationService에서 응답 필드 null 체크 시 던져지는 예외 처리
            log.error("Failed to save constellation due to invalid response state: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "별자리 저장 중 예상치 못한 응답 오류가 발생했습니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to save constellation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "별자리 저장 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getConstellationHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting constellation history for user: {}, page: {}, size: {}", userId, page, size);
        try {
            // Validate pagination parameters
            if (page < 0) {
                log.warn("Invalid page number: {}", page);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "페이지 번호는 0 이상이어야 합니다."));
            }
            if (size <= 0 || size > 100) {
                log.warn("Invalid page size: {}", size);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "페이지 크기는 1에서 100 사이여야 합니다."));
            }

            // Check if the requested user exists (history depends on user)
            if (!userRepository.existsById(userId)) {
                log.warn("User not found for history: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "사용자를 찾을 수 없습니다: " + userId));
            }

            Page<ConstellationHistoryResponse> history = constellationService.getConstellationHistory(
                userId, PageRequest.of(page, size));
            
            log.info("Found {} history records for user {} (page {}, size {})", 
                history.getTotalElements(), userId, page, size);
            
            if (history.isEmpty()) {
                log.info("No history records found for user {} (page {}, size {})", userId, page, size);
            }
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Failed to get constellation history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "별자리 히스토리를 가져오는데 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("") // Handles POST requests to /api/constellation
    public ResponseEntity<?> handleConstellationBasePost() {
        log.warn("Received POST request to /api/constellation without /save suffix. This is likely a misrouted request.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Collections.singletonMap("message", "잘못된 요청: 별자리 저장 경로는 /api/constellation/save 입니다."));
    }
} 