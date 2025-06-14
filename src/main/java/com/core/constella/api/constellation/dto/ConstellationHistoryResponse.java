package com.core.constella.api.constellation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class ConstellationHistoryResponse {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("constellationId")
    private Long constellationId;
    
    @JsonProperty("pinOrder")
    private List<String> pinOrder;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("pins")
    private List<ConstellationPinDto> pins;
} 