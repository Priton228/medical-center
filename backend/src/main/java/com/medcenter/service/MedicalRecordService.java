package com.medcenter.service;

import com.medcenter.domain.Appointment;
import com.medcenter.domain.Doctor;
import com.medcenter.domain.MedicalRecord;
import com.medcenter.domain.Patient;
import com.medcenter.dto.MedicalRecordDtos;
import com.medcenter.exception.BadRequestException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.AppointmentRepository;
import com.medcenter.repository.DoctorRepository;
import com.medcenter.repository.MedicalRecordRepository;
import com.medcenter.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository repository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<MedicalRecordDtos.MedicalRecordResponse> forPatient(Long patientId) {
        return repository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
            .map(Mappers::toMedicalRecordResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordDtos.MedicalRecordResponse> forCurrentPatient(String username) {
        Patient p = patientRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        return forPatient(p.getId());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordDtos.MedicalRecordResponse> byDoctor(String username) {
        Doctor d = doctorRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));
        return repository.findByDoctorIdOrderByCreatedAtDesc(d.getId()).stream()
            .map(Mappers::toMedicalRecordResponse).toList();
    }

    @Transactional
    public MedicalRecordDtos.MedicalRecordResponse createByDoctor(String doctorUsername, MedicalRecordDtos.MedicalRecordRequest req) {
        Doctor doctor = doctorRepository.findByUserUsername(doctorUsername)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));

        Long patientId = req.patientId();
        Appointment appointment = null;
        if (req.appointmentId() != null) {
            appointment = appointmentRepository.findById(req.appointmentId())
                .orElseThrow(() -> new NotFoundException("Запись на приём не найдена"));
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new BadRequestException("Запись принадлежит другому врачу");
            }
            patientId = appointment.getPatient().getId();
        }
        if (patientId == null) {
            throw new BadRequestException("Не указан пациент");
        }
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new NotFoundException("Пациент не найден"));

        MedicalRecord record = MedicalRecord.builder()
            .patient(patient)
            .doctor(doctor)
            .appointment(appointment)
            .diagnosis(req.diagnosis())
            .treatment(req.treatment())
            .notes(req.notes())
            .build();
        return Mappers.toMedicalRecordResponse(repository.save(record));
    }
}
