package com.core.constella.api.diary.service;

import java.util.ArrayList;
import java.util.List;

import com.core.constella.api.diary.domain.ConstellationPoint;
import com.core.constella.api.diary.dto.CreateConstellationRequest;
import com.core.constella.api.diary.repository.ConstellationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.core.constella.api.diary.domain.Constellation;
import com.core.constella.api.diary.domain.Diary;
import com.core.constella.api.diary.repository.ConstellationPointRepository;
import com.core.constella.api.diary.repository.DiaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ConstellationService {

    private final ConstellationRepository constellationRepository;
    private final ConstellationPointRepository constellationPointRepository;
    private final DiaryRepository diaryRepository;



    @Transactional
    public Constellation createConstellation(String name, List<CreateConstellationRequest.ConstellationPointRequest> pointRequests) {

        Constellation constellation = constellationRepository.findAll().stream().findFirst().orElse(null);

        if (constellation == null) {

            constellation = new Constellation();
        } else {

            constellation.getPoints().clear();
        }

        constellation.setName(name);
        List<ConstellationPoint> points = new ArrayList<>();
        int order = 0;

        for (CreateConstellationRequest.ConstellationPointRequest pointRequest : pointRequests) {
            Diary diary = diaryRepository.findById(pointRequest.getDiaryId())
                    .orElseThrow(() -> new IllegalArgumentException("Diary not found: " + pointRequest.getDiaryId()));

            ConstellationPoint point = new ConstellationPoint();
            point.setConstellation(constellation);
            point.setDiary(diary);
            point.setLatitude(pointRequest.getLatitude());
            point.setLongitude(pointRequest.getLongitude());
            point.setOrderInConstellation(order++);
            points.add(point);
        }

        constellation.setPoints(points);
        return constellationRepository.save(constellation);
    }

    public Constellation getConstellation(Long id) {
        return constellationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Constellation not found: " + id));
    }
}
