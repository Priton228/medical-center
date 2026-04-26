package com.medcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PatientDtos {

    public record PatientResponse(
        Long id,
        Long userId,
        String login,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
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
        @NotBlank @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String login,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String fullName,
        String phone,
        LocalDate birthDate,
        String address,
        String insuranceNumber
    ) {}
}
