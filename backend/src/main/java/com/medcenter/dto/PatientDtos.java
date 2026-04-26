package com.medcenter.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class PatientDtos {

    public record PatientResponse(
        Long id,
        Long userId,
        String username,
        String fullName,
        String email,
        String phone,
        LocalDate birthDate,
        String address,
        String insuranceNumber,
        boolean enabled
    ) {}

    public record UpdatePatientRequest(
        LocalDate birthDate,
        String address,
        String insuranceNumber
    ) {}

    public record CreatePatientRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String fullName,
        String phone,
        LocalDate birthDate,
        String address,
        String insuranceNumber
    ) {}
}
