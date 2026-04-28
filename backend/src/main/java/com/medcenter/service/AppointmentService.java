package com.medcenter.service;

import com.medcenter.domain.Appointment;
import com.medcenter.domain.Doctor;
import com.medcenter.domain.Patient;
import com.medcenter.domain.enums.AppointmentStatus;
import com.medcenter.dto.AppointmentDtos;
import com.medcenter.exception.BadRequestException;
import com.medcenter.exception.ConflictException;
import com.medcenter.exception.ForbiddenException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.AppointmentRepository;
import com.medcenter.repository.DoctorRepository;
import com.medcenter.repository.PatientRepository;
import com.medcenter.service.notification.AppointmentNotificationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentNotificationFacade notifications;

    @Transactional
    public AppointmentDtos.AppointmentResponse createForPatient(String patientLogin, AppointmentDtos.CreateAppointmentRequest req) {
        Patient patient = patientRepository.findByUserLogin(patientLogin)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        Doctor doctor = doctorRepository.findById(req.doctorId())
            .orElseThrow(() -> new NotFoundException("Врач не найден"));
        if (!doctor.isAvailable()) {
            throw new BadRequestException("Врач недоступен");
        }
        if (req.appointmentDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата приёма должна быть в будущем");
        }
        // Используем status-фильтрующий запрос: после миграции V4 для одного слота
        // могут существовать несколько CANCELLED-строк, поэтому findBy...AppointmentDate
        // (Optional) сломался бы IncorrectResultSizeDataAccessException.
        appointmentRepository
            .findByDoctorIdAndAppointmentDateAndStatusNot(
                doctor.getId(), req.appointmentDate(), AppointmentStatus.CANCELLED)
            .ifPresent(a -> { throw new ConflictException("Слот уже занят"); });
        Appointment a = Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .appointmentDate(req.appointmentDate())
            .status(AppointmentStatus.PLANNED)
            .notes(req.notes())
            .confirmToken(UUID.randomUUID().toString().replace("-", ""))
            .build();
        a = appointmentRepository.save(a);
        notifications.onCreated(a);
        return Mappers.toAppointmentResponse(appointmentRepository.save(a));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDtos.AppointmentResponse> listAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(Mappers::toAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDtos.AppointmentResponse> listForPatient(String login, Pageable pageable) {
        Patient p = patientRepository.findByUserLogin(login)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        return appointmentRepository.findByPatientId(p.getId(), pageable).map(Mappers::toAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDtos.AppointmentResponse> listForDoctor(String login, Pageable pageable) {
        Doctor d = doctorRepository.findByUserLogin(login)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));
        return appointmentRepository.findByDoctorId(d.getId(), pageable).map(Mappers::toAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDtos.AppointmentResponse> upcomingForPatient(String login) {
        Patient p = patientRepository.findByUserLogin(login)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        return appointmentRepository.findByPatientIdAndAppointmentDateAfter(p.getId(), LocalDateTime.now()).stream()
            .map(Mappers::toAppointmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDtos.AppointmentResponse> upcomingForDoctor(String login) {
        Doctor d = doctorRepository.findByUserLogin(login)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));
        return appointmentRepository.findByDoctorIdAndAppointmentDateAfter(d.getId(), LocalDateTime.now()).stream()
            .map(Mappers::toAppointmentResponse).toList();
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse changeStatus(Long id, AppointmentStatus newStatus, String currentLogin, boolean isAdmin) {
        Appointment a = appointmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        boolean ownedByPatient = a.getPatient().getUser().getLogin().equals(currentLogin);
        boolean ownedByDoctor = a.getDoctor().getUser().getLogin().equals(currentLogin);
        if (!isAdmin && !ownedByPatient && !ownedByDoctor) {
            throw new ForbiddenException("Нет прав на изменение записи");
        }
        if (!isAdmin && ownedByPatient && newStatus != AppointmentStatus.CANCELLED) {
            throw new ForbiddenException("Пациент может только отменить запись");
        }
        AppointmentStatus old = a.getStatus();
        // При возврате из CANCELLED в активный статус слот может быть уже занят
        // другой записью — нужно проверить вручную, чтобы вернуть осмысленный 409,
        // а не 500 от частичного индекса uq_doctor_slot_active.
        if (old == AppointmentStatus.CANCELLED && newStatus != AppointmentStatus.CANCELLED) {
            Long currentId = a.getId();
            appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatusNot(
                    a.getDoctor().getId(), a.getAppointmentDate(), AppointmentStatus.CANCELLED)
                .filter(other -> !other.getId().equals(currentId))
                .ifPresent(other -> { throw new ConflictException("Слот уже занят другой активной записью"); });
        }
        a.setStatus(newStatus);
        a = appointmentRepository.save(a);

        if (old != newStatus) {
            switch (newStatus) {
                case CANCELLED -> notifications.onCancelled(a);
                case CONFIRMED -> notifications.onConfirmed(a);
                default -> { /* PLANNED / COMPLETED — без отдельного уведомления */ }
            }
            appointmentRepository.save(a);
        }
        return Mappers.toAppointmentResponse(a);
    }

    /** Перенос приёма на другую дату. Доступен пациенту-владельцу, врачу-владельцу и админу. */
    @Transactional
    public AppointmentDtos.AppointmentResponse reschedule(Long id, AppointmentDtos.RescheduleRequest req,
                                                          String currentLogin, boolean isAdmin) {
        Appointment a = appointmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        boolean ownedByPatient = a.getPatient().getUser().getLogin().equals(currentLogin);
        boolean ownedByDoctor = a.getDoctor().getUser().getLogin().equals(currentLogin);
        if (!isAdmin && !ownedByPatient && !ownedByDoctor) {
            throw new ForbiddenException("Нет прав на перенос записи");
        }
        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Нельзя перенести завершённую или отменённую запись");
        }
        if (req.appointmentDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Новая дата должна быть в будущем");
        }
        Long currentId = a.getId();
        // findBy...AndStatusNot вернёт максимум одну запись (см. uq_doctor_slot_active).
        appointmentRepository
            .findByDoctorIdAndAppointmentDateAndStatusNot(
                a.getDoctor().getId(), req.appointmentDate(), AppointmentStatus.CANCELLED)
            .filter(other -> !other.getId().equals(currentId))
            .ifPresent(other -> { throw new ConflictException("Слот уже занят"); });

        a.setAppointmentDate(req.appointmentDate());
        if (req.notes() != null) a.setNotes(req.notes());
        a.setRescheduleCount(a.getRescheduleCount() + 1);
        a.setStatus(AppointmentStatus.PLANNED);
        a.setReminderSentAt(null);
        a.setConfirmToken(UUID.randomUUID().toString().replace("-", ""));
        a = appointmentRepository.save(a);

        notifications.onRescheduled(a);
        return Mappers.toAppointmentResponse(appointmentRepository.save(a));
    }

    /** Подтверждение по одноразовой ссылке из письма. */
    @Transactional
    public AppointmentDtos.AppointmentResponse confirmByToken(String token) {
        Appointment a = appointmentRepository.findByConfirmToken(token)
            .orElseThrow(() -> new NotFoundException("Токен подтверждения не найден или устарел"));
        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Запись уже " + a.getStatus());
        }
        a.setStatus(AppointmentStatus.CONFIRMED);
        a.setConfirmToken(null);
        a = appointmentRepository.save(a);
        notifications.onConfirmed(a);
        return Mappers.toAppointmentResponse(appointmentRepository.save(a));
    }

    /** Отклонение по одноразовой ссылке из письма (отменяет запись). */
    @Transactional
    public AppointmentDtos.AppointmentResponse rejectByToken(String token) {
        Appointment a = appointmentRepository.findByConfirmToken(token)
            .orElseThrow(() -> new NotFoundException("Токен подтверждения не найден или устарел"));
        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            return Mappers.toAppointmentResponse(a);
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setConfirmToken(null);
        a = appointmentRepository.save(a);
        notifications.onCancelled(a);
        return Mappers.toAppointmentResponse(appointmentRepository.save(a));
    }

    @Transactional(readOnly = true)
    public AppointmentDtos.AppointmentResponse get(Long id) {
        return appointmentRepository.findById(id).map(Mappers::toAppointmentResponse)
            .orElseThrow(() -> new NotFoundException("Запись не найдена"));
    }
}
