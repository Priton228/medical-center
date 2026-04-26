package com.medcenter.dto;

import java.util.List;

public class StatsDtos {

    public record OverviewStats(
        long totalUsers,
        long totalPatients,
        long totalDoctors,
        long totalAppointments,
        long plannedAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long totalSymptoms,
        long totalDiagnoses,
        List<SpecializationCount> appointmentsBySpecialization
    ) {}

    public record SpecializationCount(String specialization, long count) {}
}
