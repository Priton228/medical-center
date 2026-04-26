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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public AppointmentDtos.AppointmentResponse createForPatient(String patientUsername, AppointmentDtos.CreateAppointmentRequest req) {
        Patient patient = patientRepository.findByUserUsername(patientUsername)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        Doctor doctor = doctorRepository.findById(req.doctorId())
            .orElseThrow(() -> new NotFoundException("Врач не найден"));
        if (!doctor.isAvailable()) {
            throw new BadRequestException("Врач недоступен");
        }
        if (req.appointmentDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата приёма должна быть в будущем");
        }
        appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), req.appointmentDate())
            .ifPresent(a -> { throw new ConflictException("Слот уже занят"); });
        Appointment a = Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .appointmentDate(req.appointmentDate())
            .status(AppointmentStatus.PLANNED)
            .notes(req.notes())
            .build();
        return Mappers.toAppointmentResponse(appointmentRepository.save(a));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDtos.AppointmentResponse> listAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(Mappers::toAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDtos.AppointmentResponse> listForPatient(String username, Pageable pageable) {
        Patient p = patientRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        return appointmentRepository.findByPatientId(p.getId(), pageable).map(Mappers::toAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDtos.AppointmentResponse> listForDoctor(String username, Pageable pageable) {
        Doctor d = doctorRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));
        return appointmentRepository.findByDoctorId(d.getId(), pageable).map(Mappers::toAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDtos.AppointmentResponse> upcomingForPatient(String username) {
        Patient p = patientRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        return appointmentRepository.findByPatientIdAndAppointmentDateAfter(p.getId(), LocalDateTime.now()).stream()
            .map(Mappers::toAppointmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDtos.AppointmentResponse> upcomingForDoctor(String username) {
        Doctor d = doctorRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));
        return appointmentRepository.findByDoctorIdAndAppointmentDateAfter(d.getId(), LocalDateTime.now()).stream()
            .map(Mappers::toAppointmentResponse).toList();
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse changeStatus(Long id, AppointmentStatus newStatus, String currentUsername, boolean isAdmin) {
        Appointment a = appointmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        boolean ownedByPatient = a.getPatient().getUser().getUsername().equals(currentUsername);
        boolean ownedByDoctor = a.getDoctor().getUser().getUsername().equals(currentUsername);
        if (!isAdmin && !ownedByPatient && !ownedByDoctor) {
            throw new ForbiddenException("Нет прав на изменение записи");
        }
        if (!isAdmin && ownedByPatient && newStatus != AppointmentStatus.CANCELLED) {
            throw new ForbiddenException("Пациент может только отменить запись");
        }
        a.setStatus(newStatus);
        return Mappers.toAppointmentResponse(appointmentRepository.save(a));
    }

    @Transactional(readOnly = true)
    public AppointmentDtos.AppointmentResponse get(Long id) {
        return appointmentRepository.findById(id).map(Mappers::toAppointmentResponse)
            .orElseThrow(() -> new NotFoundException("Запись не найдена"));
    }
}
