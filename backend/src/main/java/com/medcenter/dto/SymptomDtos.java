package com.medcenter.dto;

import jakarta.validation.constraints.NotBlank;

public class SymptomDtos {
    public record SymptomResponse(Long id, String name, String description) {}

    public record SymptomRequest(
        @NotBlank String name,
        String description
    ) {}
}
