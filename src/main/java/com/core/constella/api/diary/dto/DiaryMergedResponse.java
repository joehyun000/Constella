package com.core.constella.api.diary.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private LocalDate date;
}
