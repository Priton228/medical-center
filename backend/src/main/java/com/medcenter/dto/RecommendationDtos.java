package com.medcenter.dto;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public class RecommendationDtos {

    public record AnalyzeRequest(
        @NotEmpty List<Long> symptomIds
    ) {}

    public record RecommendationResponse(
        Long id,
        Long patientId,
        List<Long> symptomIds,
        Long diagnosisId,
        String diagnosisName,
        Long recommendedDoctorId,
        String recommendedDoctorName,
        String recommendedDoctorSpecialization,
        Integer confidence,
        LocalDateTime createdAt
    ) {}
}
