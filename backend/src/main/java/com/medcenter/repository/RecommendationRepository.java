package com.medcenter.repository;

import com.medcenter.domain.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
