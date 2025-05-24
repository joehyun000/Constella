package com.core.constella.api.diary.service;

import com.core.constella.api.diary.dto.DiaryCreateRequest;
import com.core.constella.api.diary.dto.DiaryEntryResponse;
import com.core.constella.api.diary.dto.DiaryMergedResponse;
import com.core.constella.api.diary.domain.Diary;
import com.core.constella.api.diary.domain.DiaryEntry;
import com.core.constella.api.diary.domain.DiaryImage;
import com.core.constella.api.diary.repository.DiaryEntryRepository;
import com.core.constella.api.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryEntryRepository diaryEntryRepository;

    // entity 일기 생성
    @Transactional
    public void createEntry(DiaryCreateRequest request) throws IOException {
        Diary diary = diaryRepository.findByLocationCode(request.getLocationCode())
                .orElseGet(() -> Diary.builder()
                        .locationCode(request.getLocationCode())
                        .build());

        DiaryEntry entry = DiaryEntry.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .date(LocalDate.parse(request.getDate()))
                .diary(diary)
                .images(new ArrayList<>())
                .build();

        // 이미지 URL 처리 추가!
        if (request.getImageUrls() != null) {
            for (String imageUrl : request.getImageUrls()) {
                DiaryImage image = new DiaryImage();
                image.setImageUrl(imageUrl);
                image.setEntry(entry);
                entry.getImages().add(image);
            }
        }


        diary.getEntries().add(entry); // Diary에 Entry 추가

        if (diary.getId() == null) {
            diaryRepository.save(diary); // 새 다이어리일 때만 저장
        }

        diaryEntryRepository.save(entry); // Entry 저장 (명확성 위해 추가)
    }

    // entity 일기 조회
    @Transactional(readOnly = true)
    public DiaryEntryResponse getEntryById(Long entryId) {
        DiaryEntry entry = diaryEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));

        List<String> imageUrls = entry.getImages().stream()
                .map(DiaryImage::getImageUrl)
                .toList();

        return DiaryEntryResponse.builder()
                .entryId(entry.getId())
                .title(entry.getTitle())
                .date(entry.getDate())
                .contents(entry.getContents())
                .imageUrls(imageUrls)
                .build();
    }

    // 일기 병합하기
    @Transactional(readOnly = true)
    public DiaryMergedResponse mergedEntries(String locationCode) {
        Diary diary = diaryRepository.findByLocationCode(locationCode)
                .orElseThrow(() -> new RuntimeException("일기가 없습니다."));

        StringBuilder contentBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();

        for (DiaryEntry entry : diary.getEntries()) {
            contentBuilder.append("[").append(entry.getDate()).append("] ")
                    .append(entry.getTitle()).append("\n");
            for (String c : entry.getContents()) {
                contentBuilder.append(c).append("\n");
            }
            contentBuilder.append("\n");
            for (DiaryImage img : entry.getImages()) {
                images.add(img.getImageUrl());
            }
        }

        return DiaryMergedResponse.builder()
                .locationCode(locationCode)
                .mergedTitle("병합된 일기")
                .mergedContent(contentBuilder.toString())
                .imageUrls(images)
                .build();
    }
}
