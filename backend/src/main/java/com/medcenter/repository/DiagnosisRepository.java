package com.medcenter.repository;

import com.medcenter.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    List<Diagnosis> findBySpecializationIgnoreCase(String specialization);

    @Query("SELECT DISTINCT d FROM Diagnosis d JOIN d.symptoms s WHERE s.id IN :symptomIds")
    List<Diagnosis> findAllBySymptomIds(List<Long> symptomIds);
}
