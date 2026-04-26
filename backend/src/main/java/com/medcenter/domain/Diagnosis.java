package com.medcenter.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Диагноз с привязкой к специализации врача и набору связанных симптомов.
 */
@Entity
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 128)
    private String specialization;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "symptoms_diagnosis",
        joinColumns = @JoinColumn(name = "diagnosis_id"),
        inverseJoinColumns = @JoinColumn(name = "symptom_id")
    )
    private Set<Symptom> symptoms = new HashSet<>();

    public Diagnosis() {
    }

    public Diagnosis(Long id, String name, String description, String specialization, Set<Symptom> symptoms) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.specialization = specialization;
        this.symptoms = symptoms == null ? new HashSet<>() : symptoms;
    }

    public void addSymptom(Symptom symptom) {
        if (symptom == null) return;
        if (symptoms == null) symptoms = new HashSet<>();
        symptoms.add(symptom);
    }

    public void removeSymptom(Symptom symptom) {
        if (symptom == null || symptoms == null) return;
        symptoms.remove(symptom);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Set<Symptom> getSymptoms() { return symptoms; }
    public void setSymptoms(Set<Symptom> symptoms) { this.symptoms = symptoms == null ? new HashSet<>() : symptoms; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String name;
        private String description;
        private String specialization;
        private Set<Symptom> symptoms = new HashSet<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder specialization(String specialization) { this.specialization = specialization; return this; }
        public Builder symptoms(Set<Symptom> symptoms) { this.symptoms = symptoms == null ? new HashSet<>() : symptoms; return this; }

        public Diagnosis build() {
            return new Diagnosis(id, name, description, specialization, symptoms);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Diagnosis other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Diagnosis{id=" + id + ", name='" + name + "'}";
    }
}
