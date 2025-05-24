package com.core.constella.api.diary.controller;

import com.core.constella.api.diary.dto.DiaryCreateRequest;
import com.core.constella.api.diary.dto.DiaryEntryResponse;
import com.core.constella.api.diary.dto.DiaryMergedResponse;
import com.core.constella.api.diary.service.DiaryService;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createDiary(@RequestBody DiaryCreateRequest request) {
        try {
            diaryService.createEntry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("일기 작성 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("작성 실패 : " + e.getMessage());

        }

    }

    // entry 단일 조회 API
    @GetMapping("/entry/{entryId}")
    public ResponseEntity<DiaryEntryResponse> getEntry(@PathVariable Long entryId) {
        DiaryEntryResponse response = diaryService.getEntryById(entryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/merge/{locationCode}")
    public ResponseEntity<String> mergeDiary(@PathVariable String locationCode) {
        DiaryMergedResponse response = diaryService.mergedEntries(locationCode);
        return ResponseEntity.ok(response.getMergedContent());
    }
}
