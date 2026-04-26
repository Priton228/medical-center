package com.medcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class DoctorDtos {

    public record DoctorResponse(
        Long id,
        Long userId,
        String username,
        String fullName,
        String email,
        String phone,
        String specialization,
        String bio,
        String photoUrl,
        boolean available,
        LocalTime workStart,
        LocalTime workEnd,
        String roomNumber
    ) {}

    public record CreateDoctorRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String fullName,
        String phone,
        @NotBlank String specialization,
        String bio,
        String photoUrl,
        String roomNumber,
        LocalTime workStart,
        LocalTime workEnd,
        Boolean available
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
}
