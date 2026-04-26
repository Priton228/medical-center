package com.medcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcenter.domain.*;
import com.medcenter.dto.RecommendationDtos;
import com.medcenter.exception.BadRequestException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Facade над механизмом рекомендаций: принимает симптомы пациента,
 * подбирает диагноз через {@link SymptomMatchingStrategy}, выбирает врача
 * соответствующей специализации и сохраняет историю в БД.
 */
@Service
public class RecommendationService {
    public RecommendationService(SymptomRepository symptomRepository, DiagnosisRepository diagnosisRepository, DoctorRepository doctorRepository, PatientRepository patientRepository, RecommendationRepository recommendationRepository, SymptomMatchingStrategy matchingStrategy) {
        this.symptomRepository = symptomRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.recommendationRepository = recommendationRepository;
        this.matchingStrategy = matchingStrategy;
    }


    private final SymptomRepository symptomRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final RecommendationRepository recommendationRepository;
    private final SymptomMatchingStrategy matchingStrategy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public RecommendationDtos.RecommendationResponse analyze(String patientUsername, List<Long> symptomIds) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            throw new BadRequestException("Список симптомов не должен быть пустым");
        }
        Patient patient = patientRepository.findByUserUsername(patientUsername)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));

        List<Symptom> symptoms = symptomRepository.findAllById(symptomIds);
        if (symptoms.isEmpty()) {
            throw new BadRequestException("Указанные симптомы не найдены");
        }

        List<Diagnosis> candidates = diagnosisRepository.findAllBySymptomIds(symptomIds);
        Optional<SymptomMatchingStrategy.Match> bestMatch = matchingStrategy.match(symptomIds, candidates);

        Diagnosis diagnosis = bestMatch.map(SymptomMatchingStrategy.Match::diagnosis).orElse(null);
        Doctor doctor = null;
        if (diagnosis != null) {
            doctor = doctorRepository.findBySpecializationIgnoreCase(diagnosis.getSpecialization()).stream()
                .filter(Doctor::isAvailable)
                .min(Comparator.comparing(Doctor::getId))
                .orElse(null);
        }
        int confidence = bestMatch.map(SymptomMatchingStrategy.Match::confidence).orElse(0);

        Recommendation rec = Recommendation.builder()
            .patient(patient)
            .symptomsJson(toJson(symptomIds))
            .diagnosis(diagnosis)
            .recommendedDoctor(doctor)
            .confidence(confidence)
            .build();
        rec = recommendationRepository.save(rec);
        return Mappers.toRecommendationResponse(rec, symptomIds);
    }

    @Transactional(readOnly = true)
    public List<RecommendationDtos.RecommendationResponse> historyForPatient(String patientUsername) {
        Patient p = patientRepository.findByUserUsername(patientUsername)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        return recommendationRepository.findByPatientIdOrderByCreatedAtDesc(p.getId()).stream()
            .map(r -> Mappers.toRecommendationResponse(r, fromJson(r.getSymptomsJson())))
            .toList();
    }

    private String toJson(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<Long> fromJson(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (Exception e) {
            return List.of();
        }
    }
}
