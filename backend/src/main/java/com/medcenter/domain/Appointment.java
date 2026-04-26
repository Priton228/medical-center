package com.medcenter.domain;

import com.medcenter.domain.enums.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Запись пациента на приём к врачу. Уникальный слот: {@code (doctor_id, appointment_date)}.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status = AppointmentStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Appointment() {
    }

    public Appointment(Long id, Patient patient, Doctor doctor, LocalDateTime appointmentDate,
                       AppointmentStatus status, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.status = status != null ? status : AppointmentStatus.PLANNED;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AppointmentStatus.PLANNED;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Patient patient;
        private Doctor doctor;
        private LocalDateTime appointmentDate;
        private AppointmentStatus status = AppointmentStatus.PLANNED;
        private String notes;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder patient(Patient patient) { this.patient = patient; return this; }
        public Builder doctor(Doctor doctor) { this.doctor = doctor; return this; }
        public Builder appointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; return this; }
        public Builder status(AppointmentStatus status) { this.status = status; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Appointment build() {
            return new Appointment(id, patient, doctor, appointmentDate, status, notes, createdAt);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Appointment{id=" + id + ", date=" + appointmentDate + ", status=" + status + "}";
    }
}
