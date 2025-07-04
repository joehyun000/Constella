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

    public static Map<String, CountryInfo> COUNTRY_INFO_MAP;
    private static Map<String, String> COUNTRY_NAME_MAP_EN_TO_KO;
    private static Map<String, String> COUNTRY_CODE_TO_KO;

    public static class CountryInfo {
        public String nameEn;
        public double lat;
        public double lng;
        public String code;
        public CountryInfo(String nameEn, double lat, double lng, String code) {
            this.nameEn = nameEn;
            this.lat = lat;
            this.lng = lng;
            this.code = code;
        }
    }

    static {
        try (InputStream is = CountryService.class.getResourceAsStream("/countries.json")) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            org.json.JSONObject obj = new org.json.JSONObject(json);
            Map<String, CountryInfo> infoMap = new HashMap<>();
            Map<String, String> enToKo = new HashMap<>();
            Map<String, String> codeToKo = new HashMap<>();
            for (String nameKo : obj.keySet()) {
                org.json.JSONObject v = obj.getJSONObject(nameKo);
                String nameEn = v.getString("nameEn");
                double lat = v.getDouble("lat");
                double lng = v.getDouble("lng");
                String code = v.has("code") ? v.getString("code") : null;
                infoMap.put(nameKo, new CountryInfo(nameEn, lat, lng, code));
                enToKo.put(nameEn, nameKo);
                if (code != null) codeToKo.put(code, nameKo);
            }
            COUNTRY_INFO_MAP = infoMap;
            COUNTRY_NAME_MAP_EN_TO_KO = enToKo;
            COUNTRY_CODE_TO_KO = codeToKo;
            System.out.println("COUNTRY_INFO_MAP keys: " + COUNTRY_INFO_MAP.keySet());
        } catch (Exception e) {
            COUNTRY_INFO_MAP = Map.of();
            COUNTRY_NAME_MAP_EN_TO_KO = Map.of();
            COUNTRY_CODE_TO_KO = Map.of();
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

    public static Map<String, String> getCountryNameKoByCode() {
        return COUNTRY_CODE_TO_KO;
    }
} 