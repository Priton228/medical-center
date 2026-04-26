package com.medcenter.dto;

import com.medcenter.domain.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public class AuthDtos {

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 64) String password,
        @NotBlank @Size(max = 128) String fullName,
        String phone,
        @NotNull RoleName role,
        // optional patient fields
        LocalDate birthDate,
        String address,
        String insuranceNumber,
        // optional doctor fields
        String specialization,
        String bio
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
