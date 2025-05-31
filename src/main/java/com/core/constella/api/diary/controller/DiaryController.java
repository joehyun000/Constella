package com.core.constella.api.diary.controller;

import com.core.constella.api.diary.dto.DiaryCreateRequest;
import com.core.constella.api.diary.dto.DiaryMergedResponse;
import com.core.constella.api.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createDiary(@ModelAttribute DiaryCreateRequest request) {
        try {
            diaryService.createEntry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("일기 작성 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("작성 실패 : " + e.getMessage());

        }

    }
//    @GetMapping("/merge/{locationCode}")
//    public ResponseEntity<String> mergeDiary(@PathVariable String locationCode) {
//        //return ResponseEntity.ok(diaryService.Merged);
//        return null;
//    }

    //다이어리 리스트 받아오는 메서드 추가
    @GetMapping("/merge/{locationCode}")
    public ResponseEntity<List<DiaryMergedResponse>> getDiariesByCountry(@PathVariable String locationCode) {
        List<DiaryMergedResponse> diaries = diaryService.getMergedEntries(locationCode);
        return ResponseEntity.ok(diaries);
    }

}
