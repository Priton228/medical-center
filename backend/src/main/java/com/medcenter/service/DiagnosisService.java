package com.medcenter.service;

import com.medcenter.domain.Diagnosis;
import com.medcenter.domain.Symptom;
import com.medcenter.dto.DiagnosisDtos;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.DiagnosisRepository;
import com.medcenter.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final SymptomRepository symptomRepository;

    @Transactional(readOnly = true)
    public List<DiagnosisDtos.DiagnosisResponse> listAll() {
        return diagnosisRepository.findAll().stream().map(Mappers::toDiagnosisResponse).toList();
    }

    @Transactional(readOnly = true)
    public DiagnosisDtos.DiagnosisResponse get(Long id) {
        return diagnosisRepository.findById(id).map(Mappers::toDiagnosisResponse)
            .orElseThrow(() -> new NotFoundException("Диагноз не найден"));
    }

    @Transactional
    public DiagnosisDtos.DiagnosisResponse create(DiagnosisDtos.DiagnosisRequest req) {
        Diagnosis d = Diagnosis.builder()
            .name(req.name())
            .description(req.description())
            .specialization(req.specialization())
            .build();
        if (req.symptomIds() != null) {
            d.setSymptoms(new HashSet<>(symptomRepository.findAllById(req.symptomIds())));
        }
        return Mappers.toDiagnosisResponse(diagnosisRepository.save(d));
    }

    @Transactional
    public DiagnosisDtos.DiagnosisResponse update(Long id, DiagnosisDtos.DiagnosisRequest req) {
        Diagnosis d = diagnosisRepository.findById(id).orElseThrow(() -> new NotFoundException("Диагноз не найден"));
        d.setName(req.name());
        d.setDescription(req.description());
        d.setSpecialization(req.specialization());
        if (req.symptomIds() != null) {
            List<Symptom> symptoms = symptomRepository.findAllById(req.symptomIds());
            d.setSymptoms(new HashSet<>(symptoms));
        }
        return Mappers.toDiagnosisResponse(diagnosisRepository.save(d));
    }

    @Transactional
    public void delete(Long id) {
        if (!diagnosisRepository.existsById(id)) throw new NotFoundException("Диагноз не найден");
        diagnosisRepository.deleteById(id);
    }
}
