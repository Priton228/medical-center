package com.medcenter.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private Set<Symptom> symptoms = new HashSet<>();
}
