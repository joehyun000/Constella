package com.core.constella.api.diary.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 글생성용 DTO
public class DiaryCreateRequest {
    private String locationCode;
    private String title;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private List<String> contents;
    private List<MultipartFile> images;

    // 위도
    private Double latitude;

    // 경도
    private Double longitude;
}
