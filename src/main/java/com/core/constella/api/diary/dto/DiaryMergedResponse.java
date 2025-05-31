package com.core.constella.api.diary.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 병합 결과 반환 DTO
public class DiaryMergedResponse {
    private Long id;
    private String locationCode;
    private String mergedTitle;
    private String mergedContent;
    private List<String> imageUrls;
}
