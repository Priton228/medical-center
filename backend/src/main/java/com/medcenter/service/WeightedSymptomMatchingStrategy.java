package com.medcenter.service;

import com.medcenter.domain.Diagnosis;
import com.medcenter.domain.Symptom;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация: для каждого диагноза считаем долю совпавших симптомов
 * и выбираем диагноз с наибольшим уровнем совпадения.
 */
@Component
public class WeightedSymptomMatchingStrategy implements SymptomMatchingStrategy {

    @Override
    public Optional<Match> match(List<Long> symptomIds, List<Diagnosis> candidates) {
        if (symptomIds == null || symptomIds.isEmpty() || candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        Set<Long> requested = symptomIds.stream().collect(Collectors.toUnmodifiableSet());

        Match best = null;
        for (Diagnosis d : candidates) {
            Set<Long> dSymptomIds = d.getSymptoms().stream().map(Symptom::getId).collect(Collectors.toUnmodifiableSet());
            if (dSymptomIds.isEmpty()) continue;
            long matched = dSymptomIds.stream().filter(requested::contains).count();
            if (matched == 0) continue;
            int total = dSymptomIds.size();
            // confidence = (matched / total) * 100, скорректировано на покрытие запрошенных
            int confidence = (int) Math.round(((double) matched / total) * 70.0
                + ((double) matched / requested.size()) * 30.0);
            if (best == null || confidence > best.confidence()) {
                best = new Match(d, (int) matched, total, confidence);
            }
        }
        return Optional.ofNullable(best);
    }
}
