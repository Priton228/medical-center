package com.medcenter.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class DiagnosisDtos {
    public record DiagnosisResponse(
        Long id,
        String name,
        String description,
        String specialization,
        List<Long> symptomIds
    ) {}

    public record DiagnosisRequest(
        @NotBlank String name,
        String description,
        @NotBlank String specialization,
        List<Long> symptomIds
    ) {}
}
