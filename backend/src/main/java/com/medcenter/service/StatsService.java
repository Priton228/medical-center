package com.medcenter.service;

import com.medcenter.domain.Appointment;
import com.medcenter.domain.enums.AppointmentStatus;
import com.medcenter.dto.StatsDtos;
import com.medcenter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final SymptomRepository symptomRepository;
    private final DiagnosisRepository diagnosisRepository;

    @Transactional(readOnly = true)
    public StatsDtos.OverviewStats overview() {
        List<Appointment> all = appointmentRepository.findAll();
        Map<String, Long> bySpec = all.stream()
            .collect(Collectors.groupingBy(a -> a.getDoctor().getSpecialization(), Collectors.counting()));
        List<StatsDtos.SpecializationCount> spec = bySpec.entrySet().stream()
            .map(e -> new StatsDtos.SpecializationCount(e.getKey(), e.getValue()))
            .sorted((a, b) -> Long.compare(b.count(), a.count()))
            .toList();

        return new StatsDtos.OverviewStats(
            userRepository.count(),
            patientRepository.count(),
            doctorRepository.count(),
            all.size(),
            appointmentRepository.countByStatus(AppointmentStatus.PLANNED),
            appointmentRepository.countByStatus(AppointmentStatus.COMPLETED),
            appointmentRepository.countByStatus(AppointmentStatus.CANCELLED),
            symptomRepository.count(),
            diagnosisRepository.count(),
            spec
        );
    }
}
