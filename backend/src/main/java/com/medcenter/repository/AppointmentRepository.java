package com.medcenter.repository;

import com.medcenter.domain.Appointment;
import com.medcenter.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDateTime from, LocalDateTime to);
    List<Appointment> findByPatientIdAndAppointmentDateAfter(Long patientId, LocalDateTime after);
    List<Appointment> findByDoctorIdAndAppointmentDateAfter(Long doctorId, LocalDateTime after);
    Optional<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDateTime appointmentDate);
    Optional<Appointment> findByConfirmToken(String token);
    long countByStatus(AppointmentStatus status);

    List<Appointment> findByStatusInAndAppointmentDateBetweenAndReminderSentAtIsNull(
        java.util.Collection<AppointmentStatus> statuses,
        LocalDateTime from,
        LocalDateTime to
    );
}
