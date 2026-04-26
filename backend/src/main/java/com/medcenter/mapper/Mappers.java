package com.medcenter.mapper;

import com.medcenter.domain.*;
import com.medcenter.dto.*;

import java.util.List;
import java.util.stream.Collectors;

public final class Mappers {
    private Mappers() {}

    public static UserDtos.UserResponse toUserResponse(User u) {
        return new UserDtos.UserResponse(
            u.getId(), u.getLogin(), u.getEmail(), u.getFullName(), u.getPhone(),
            u.getAvatarUrl(), u.isEnabled(),
            u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
            u.getCreatedAt()
        );
    }

    public static DoctorDtos.DoctorResponse toDoctorResponse(Doctor d) {
        User u = d.getUser();
        return new DoctorDtos.DoctorResponse(
            d.getId(), u.getId(), u.getLogin(), u.getFullName(), u.getEmail(), u.getPhone(),
            u.getAvatarUrl(),
            d.getSpecialization(), d.getBio(), d.getPhotoUrl(), d.isAvailable(),
            d.getWorkStart(), d.getWorkEnd(), d.getRoomNumber()
        );
    }

    public static PatientDtos.PatientResponse toPatientResponse(Patient p) {
        User u = p.getUser();
        return new PatientDtos.PatientResponse(
            p.getId(), u.getId(), u.getLogin(), u.getFullName(), u.getEmail(), u.getPhone(),
            u.getAvatarUrl(),
            p.getBirthDate(), p.getAddress(), p.getInsuranceNumber(), u.isEnabled()
        );
    }

    public static AppointmentDtos.AppointmentResponse toAppointmentResponse(Appointment a) {
        Patient p = a.getPatient();
        Doctor d = a.getDoctor();
        return new AppointmentDtos.AppointmentResponse(
            a.getId(),
            p.getId(), p.getUser().getFullName(),
            d.getId(), d.getUser().getFullName(), d.getSpecialization(),
            a.getAppointmentDate(), a.getStatus(), a.getNotes(),
            a.getCalendarEventId(), a.getRescheduleCount(), a.getCreatedAt()
        );
    }

    public static SymptomDtos.SymptomResponse toSymptomResponse(Symptom s) {
        return new SymptomDtos.SymptomResponse(s.getId(), s.getName(), s.getDescription());
    }

    public static DiagnosisDtos.DiagnosisResponse toDiagnosisResponse(Diagnosis d) {
        List<Long> symptomIds = d.getSymptoms().stream().map(Symptom::getId).sorted().toList();
        return new DiagnosisDtos.DiagnosisResponse(
            d.getId(), d.getName(), d.getDescription(), d.getSpecialization(), symptomIds
        );
    }

    public static RecommendationDtos.RecommendationResponse toRecommendationResponse(Recommendation r, List<Long> symptomIds) {
        Diagnosis dg = r.getDiagnosis();
        Doctor doc = r.getRecommendedDoctor();
        return new RecommendationDtos.RecommendationResponse(
            r.getId(), r.getPatient().getId(), symptomIds,
            dg != null ? dg.getId() : null,
            dg != null ? dg.getName() : null,
            doc != null ? doc.getId() : null,
            doc != null ? doc.getUser().getFullName() : null,
            doc != null ? doc.getSpecialization() : null,
            r.getConfidence(),
            r.getCreatedAt()
        );
    }

    public static MedicalRecordDtos.MedicalRecordResponse toMedicalRecordResponse(MedicalRecord m) {
        return new MedicalRecordDtos.MedicalRecordResponse(
            m.getId(),
            m.getPatient().getId(), m.getPatient().getUser().getFullName(),
            m.getDoctor().getId(), m.getDoctor().getUser().getFullName(),
            m.getAppointment() != null ? m.getAppointment().getId() : null,
            m.getDiagnosis(), m.getTreatment(), m.getNotes(), m.getCreatedAt()
        );
    }
}
