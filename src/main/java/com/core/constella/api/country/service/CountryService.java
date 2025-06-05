package com.core.constella.api.country.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.core.constella.api.country.domain.Country;
import com.core.constella.api.country.dto.CountryDto;
import com.core.constella.api.country.repository.CountryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository countryRepository;

    private static Map<String, CountryInfo> COUNTRY_INFO_MAP;
    private static Map<String, String> COUNTRY_NAME_MAP_EN_TO_KO;

    public static class CountryInfo {
        public String nameEn;
        public double lat;
        public double lng;
        public CountryInfo(String nameEn, double lat, double lng) {
            this.nameEn = nameEn;
            this.lat = lat;
            this.lng = lng;
        }
    }

    static {
        try (InputStream is = CountryService.class.getResourceAsStream("/countries.json")) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            org.json.JSONObject obj = new org.json.JSONObject(json);
            Map<String, CountryInfo> infoMap = new HashMap<>();
            Map<String, String> enToKo = new HashMap<>();
            for (String nameKo : obj.keySet()) {
                org.json.JSONObject v = obj.getJSONObject(nameKo);
                String nameEn = v.getString("nameEn");
                double lat = v.getDouble("lat");
                double lng = v.getDouble("lng");
                infoMap.put(nameKo, new CountryInfo(nameEn, lat, lng));
                enToKo.put(nameEn, nameKo);
            }
            COUNTRY_INFO_MAP = infoMap;
            COUNTRY_NAME_MAP_EN_TO_KO = enToKo;
            System.out.println("COUNTRY_INFO_MAP keys: " + COUNTRY_INFO_MAP.keySet());
        } catch (Exception e) {
            COUNTRY_INFO_MAP = Map.of();
            COUNTRY_NAME_MAP_EN_TO_KO = Map.of();
            e.printStackTrace();
        }
    }

    public CountryDto addCountry(String nameKo, Double lat, Double lng) {
        if (nameKo == null || nameKo.isBlank()) {
            throw new IllegalArgumentException("나라 이름(nameKo)은 필수입니다.");
        }
        if (countryRepository.existsByNameKo(nameKo)) {
            throw new IllegalArgumentException("이미 존재하는 나라입니다.");
        }
        CountryInfo info = COUNTRY_INFO_MAP.get(nameKo);
        if (info == null) {
            throw new IllegalArgumentException("해당 나라 정보가 countries.json에 없습니다: " + nameKo);
        }
        String nameEn = info.nameEn;
        if (lat == null) lat = info.lat;
        if (lng == null) lng = info.lng;
        Country country = Country.builder().nameKo(nameKo).nameEn(nameEn).lat(lat).lng(lng).build();
        country = countryRepository.save(country);
        return CountryDto.builder().id(country.getId()).nameKo(country.getNameKo()).nameEn(country.getNameEn()).lat(country.getLat()).lng(country.getLng()).build();
    }

    public List<CountryDto> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(c -> CountryDto.builder()
                        .id(c.getId())
                        .nameKo(c.getNameKo())
                        .nameEn(c.getNameEn())
                        .lat(c.getLat())
                        .lng(c.getLng())
                        .build())
                .collect(Collectors.toList());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    public static Map<String, String> getCountryNameMapKoByEn() {
        return COUNTRY_NAME_MAP_EN_TO_KO;
    }
} 