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
    /**
     * Поиск активного (не отменённого) приёма по слоту.
     * Гарантированно возвращает не более одной записи благодаря частичному
     * уникальному индексу {@code uq_doctor_slot_active} (см. миграцию V4).
     *
     * <p>Безусловный аналог без status-фильтра убран намеренно: после V4
     * на одном слоте может существовать несколько строк в статусе
     * {@code CANCELLED}, и {@code Optional} такой запрос ломал бы
     * {@code IncorrectResultSizeDataAccessException}.</p>
     */
    Optional<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(
        Long doctorId, LocalDateTime appointmentDate, AppointmentStatus status);
    Optional<Appointment> findByConfirmToken(String token);
    long countByStatus(AppointmentStatus status);

    List<Appointment> findByStatusInAndAppointmentDateBetweenAndReminderSentAtIsNull(
        java.util.Collection<AppointmentStatus> statuses,
        LocalDateTime from,
        LocalDateTime to
    );
}
