package com.medcenter.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * История запросов «анализ симптомов» от пациента: какие симптомы выбрал,
 * какой диагноз и врач были рекомендованы, с какой уверенностью.
 */
@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "symptoms_json", nullable = false, columnDefinition = "TEXT")
    private String symptomsJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id")
    private Diagnosis diagnosis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_doctor_id")
    private Doctor recommendedDoctor;

    @Column(nullable = false)
    private Integer confidence = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Recommendation() {
    }

    public Recommendation(Long id, Patient patient, String symptomsJson,
                          Diagnosis diagnosis, Doctor recommendedDoctor,
                          Integer confidence, LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.symptomsJson = symptomsJson;
        this.diagnosis = diagnosis;
        this.recommendedDoctor = recommendedDoctor;
        this.confidence = confidence != null ? confidence : 0;
        this.createdAt = createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (confidence == null) {
            confidence = 0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getSymptomsJson() { return symptomsJson; }
    public void setSymptomsJson(String symptomsJson) { this.symptomsJson = symptomsJson; }

    public Diagnosis getDiagnosis() { return diagnosis; }
    public void setDiagnosis(Diagnosis diagnosis) { this.diagnosis = diagnosis; }

    public Doctor getRecommendedDoctor() { return recommendedDoctor; }
    public void setRecommendedDoctor(Doctor recommendedDoctor) { this.recommendedDoctor = recommendedDoctor; }

    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Patient patient;
        private String symptomsJson;
        private Diagnosis diagnosis;
        private Doctor recommendedDoctor;
        private Integer confidence = 0;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder patient(Patient patient) { this.patient = patient; return this; }
        public Builder symptomsJson(String symptomsJson) { this.symptomsJson = symptomsJson; return this; }
        public Builder diagnosis(Diagnosis diagnosis) { this.diagnosis = diagnosis; return this; }
        public Builder recommendedDoctor(Doctor recommendedDoctor) { this.recommendedDoctor = recommendedDoctor; return this; }
        public Builder confidence(Integer confidence) { this.confidence = confidence; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Recommendation build() {
            return new Recommendation(id, patient, symptomsJson, diagnosis, recommendedDoctor, confidence, createdAt);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recommendation other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Recommendation{id=" + id + ", confidence=" + confidence + "}";
    }
}
