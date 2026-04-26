package com.medcenter.dto;

import com.medcenter.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AppointmentDtos {

    public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientFullName,
        Long doctorId,
        String doctorFullName,
        String doctorSpecialization,
        LocalDateTime appointmentDate,
        AppointmentStatus status,
        String notes,
        LocalDateTime createdAt
    ) {}

    public record CreateAppointmentRequest(
        @NotNull Long doctorId,
        @NotNull LocalDateTime appointmentDate,
        String notes
    ) {}

    public record UpdateStatusRequest(@NotNull AppointmentStatus status) {}
}
