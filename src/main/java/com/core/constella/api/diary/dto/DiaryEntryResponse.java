package com.core.constella.api.diary.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DiaryEntryResponse {
    private Long entryId;
    private String title;
    private LocalDate date;
    private List<String> contents;
    private List<String> imageUrls;
}
