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

import java.io.File;
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
    private static final String UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads", "images").toString();




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
                    String originalFileName = file.getOriginalFilename();
                    String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    String fileName = UUID.randomUUID() + extension;

                    Path imagePath = Paths.get(UPLOAD_DIR, fileName);
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
                    .date(entry.getDate())
                    .build();

            responseList.add(response);
        }

        return responseList;
    }



}
