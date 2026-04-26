package com.medcenter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public class AuthDtos {

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    /**
     * Самостоятельная регистрация — только для пациентов.
     * Врачей и администраторов заводит только администратор через админку.
     */
    public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 64) String password,
        @NotBlank @Size(max = 128) String fullName,
        String phone,
        LocalDate birthDate,
        String address,
        String insuranceNumber
    ) {}

    public record JwtResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String username,
        String fullName,
        Set<String> roles
    ) {}
}
