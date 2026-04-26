package com.medcenter.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class MedicalRecordDtos {

    public record MedicalRecordResponse(
        Long id,
        Long patientId,
        String patientFullName,
        Long doctorId,
        String doctorFullName,
        Long appointmentId,
        String diagnosis,
        String treatment,
        String notes,
        LocalDateTime createdAt
    ) {}

    public record MedicalRecordRequest(
        Long patientId,
        Long appointmentId,
        @NotBlank String diagnosis,
        String treatment,
        String notes
    ) {}
}
