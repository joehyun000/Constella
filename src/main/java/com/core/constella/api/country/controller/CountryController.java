package com.core.constella.api.country.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.core.constella.api.country.dto.CountryDto;
import com.core.constella.api.country.service.CountryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;

    @PostMapping
    public ResponseEntity<CountryDto> addCountry(@RequestBody CountryDto dto) {
        CountryDto created = countryService.addCountry(dto.getNameKo(), dto.getLat(), dto.getLng());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }
} 