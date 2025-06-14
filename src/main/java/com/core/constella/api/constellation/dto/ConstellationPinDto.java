package com.core.constella.api.constellation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstellationPinDto {
    @JsonProperty("locationCode")
    private String locationCode;
    
    @JsonProperty("nameKo")
    private String nameKo;
    
    @JsonProperty("lat")
    private Double lat;
    
    @JsonProperty("lng")
    private Double lng;
} 