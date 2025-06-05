package com.core.constella.api.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateConstellationRequest {
    @NotBlank
    private String name;

    @NotEmpty
    private List<ConstellationPointRequest> points;

    @Getter
    @Setter
    public static class ConstellationPointRequest {
        private Long diaryId;
        private Double latitude;
        private Double longitude;
    }
}