package com.core.constella.api.diary.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.core.constella.api.diary.dto.DiaryCreateRequest;
import com.core.constella.api.diary.dto.DiaryMergedResponse;
import com.core.constella.api.diary.service.DiaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createDiary(@ModelAttribute DiaryCreateRequest request) {
        try {
            System.out.println("Received diary creation request");
            diaryService.createEntry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("일기 작성 완료");
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("입력값 오류: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File processing error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 처리 중 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("작성 실패: " + e.getMessage());
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

    // 모든 locationCode의 모든 카드(일기) 리스트를 반환하는 엔드포인트 추가
    @GetMapping("/all")
    public ResponseEntity<List<DiaryMergedResponse>> getAllDiaries() {
        return ResponseEntity.ok(diaryService.getAllMergedEntries());
    }

}
