package com.core.constella.api.diary.service;

import com.core.constella.api.diary.dto.DiaryCreateRequest;
import com.core.constella.api.diary.dto.DiaryMergedResponse;
import com.core.constella.api.diary.domain.Diary;
import com.core.constella.api.diary.domain.DiaryEntry;
import com.core.constella.api.diary.domain.DiaryImage;
import com.core.constella.api.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;

    //로컬에서 할거라 이렇게해서 이미지 가져올 예정
    private static final String UPLOAD_DIR = "C:/Users/sjun/study/constella/src/main/resources/static/images/";

    @Transactional
    public void cretaeEntry(DiaryCreateRequest request) throws IOException {
        Diary diary = diaryRepository.findByLocationCode(request.getLocationCode())
                .orElse(Diary.builder().locationCode(request.getLocationCode()).build());
        DiaryEntry entry = DiaryEntry.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .date(LocalDate.parse(request.getDate()))
                //자바의 날짜 타입으로 바꿔줌
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
    }

    public DiaryMergedResponse mergedEntries(String locationCode) {
        // locationCode에 해당하는 다이어리를 조회
        Diary diary = diaryRepository.findByLocationCode(locationCode)
                .orElseThrow(() -> new RuntimeException("일기가 없습니다."));

        StringBuilder contentBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();

        //하나의 Diary 객체에는 여러개의 DiaryEntry가 있음
        // 각각의 일기를 순회하면서 병합작업을 진행
        // [[2025-05-20] 나의 첫 번째 일기] 이런식으로 만들어짐
        for (DiaryEntry entry : diary.getEntries()) {
            contentBuilder.append("[" + entry.getDate() + "] ").append(entry.getTitle()).append("\n");
            for (String c : entry.getContents()) {
                contentBuilder.append(c).append("\n");
            }
            contentBuilder.append("\n");
            for (DiaryImage img : entry.getImages()) {
                images.add(img.getImageUrl());
            }
        }

        // 병합 결과를 DiaryMergedResponse 객체로 만들어 변환
        return DiaryMergedResponse.builder()
                .locationCode(locationCode)
                .mergedTitle("병합된 일기")
                .mergedContent(contentBuilder.toString())
                .imageUrls(images)
                .build();
    }


}
