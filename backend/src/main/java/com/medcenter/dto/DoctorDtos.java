package com.medcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public class DoctorDtos {

    public record DoctorResponse(
        Long id,
        Long userId,
        String login,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        String specialization,
        String bio,
        String photoUrl,
        boolean available,
        LocalTime workStart,
        LocalTime workEnd,
        String roomNumber
    ) {}

    public record CreateDoctorRequest(
        @NotBlank @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String login,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String fullName,
        String phone,
        @NotBlank String specialization,
        String bio,
        String roomNumber
    ) {}

    public record UpdateDoctorRequest(
        @NotBlank String specialization,
        String bio,
        String photoUrl,
        @NotNull Boolean available,
        @NotNull LocalTime workStart,
        @NotNull LocalTime workEnd,
        String roomNumber
    ) {}

    public record UpdateScheduleRequest(
        @NotNull LocalTime workStart,
        @NotNull LocalTime workEnd,
        @NotNull Boolean available
    ) {}

    /** Самостоятельное обновление профиля врача (биография, кабинет, фото). */
    public record UpdateOwnProfileRequest(
        String bio,
        String photoUrl,
        String roomNumber
    ) {}
}
