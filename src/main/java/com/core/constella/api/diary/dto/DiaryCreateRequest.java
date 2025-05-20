package com.core.constella.api.diary.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
    private String date; //"2025-05-19 형식"
    private List<String> contents;
    private List<MultipartFile> images;
}
