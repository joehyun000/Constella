package com.core.constella.api.diary.controller;

import com.core.constella.api.diary.domain.Constellation;
import com.core.constella.api.diary.dto.CreateConstellationRequest;
import com.core.constella.api.diary.service.ConstellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/constellations")
@RequiredArgsConstructor
public class ConstellationController {
    private final ConstellationService constellationService;


    //새로운 별자리를 생성
    // name과 points(일기 id, 위도/경도)를 받음
    @PostMapping
    public ResponseEntity<Constellation> createConstellation(
            @Valid @RequestBody CreateConstellationRequest request) {
        Constellation constellation = constellationService.createConstellation(
                request.getName(),
                request.getPoints()
        );
        return ResponseEntity.ok(constellation);
    }


    //ID 기반으로 하나의 별자리를 조회
    @GetMapping("/{constellationId}")
    public ResponseEntity<Constellation> getConstellation(
            @PathVariable Long constellationId) {
        Constellation constellation = constellationService.getConstellation(constellationId);
        return ResponseEntity.ok(constellation);
    }

}