package com.medcenter.service;

import com.medcenter.domain.Diagnosis;

import java.util.List;
import java.util.Optional;

/**
 * Strategy для подбора наиболее вероятного диагноза по списку симптомов.
 */
public interface SymptomMatchingStrategy {
    Optional<Match> match(List<Long> symptomIds, List<Diagnosis> candidates);

    record Match(Diagnosis diagnosis, int matchedSymptoms, int totalSymptoms, int confidence) {}
}
