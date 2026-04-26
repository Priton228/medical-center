package com.medcenter.service;

import com.medcenter.domain.Symptom;
import com.medcenter.dto.SymptomDtos;
import com.medcenter.exception.ConflictException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.SymptomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SymptomService {
    public SymptomService(SymptomRepository symptomRepository) {
        this.symptomRepository = symptomRepository;
    }


    private final SymptomRepository symptomRepository;

    @Transactional(readOnly = true)
    public List<SymptomDtos.SymptomResponse> listAll() {
        return symptomRepository.findAll().stream().map(Mappers::toSymptomResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SymptomDtos.SymptomResponse> search(String q) {
        return symptomRepository.findByNameContainingIgnoreCaseOrderByName(q).stream()
            .map(Mappers::toSymptomResponse).toList();
    }

    @Transactional
    public SymptomDtos.SymptomResponse create(SymptomDtos.SymptomRequest req) {
        symptomRepository.findByNameIgnoreCase(req.name()).ifPresent(s -> {
            throw new ConflictException("Симптом уже существует");
        });
        Symptom s = symptomRepository.save(
            Symptom.builder().name(req.name()).description(req.description()).build()
        );
        return Mappers.toSymptomResponse(s);
    }

    @Transactional
    public SymptomDtos.SymptomResponse update(Long id, SymptomDtos.SymptomRequest req) {
        Symptom s = symptomRepository.findById(id).orElseThrow(() -> new NotFoundException("Симптом не найден"));
        s.setName(req.name());
        s.setDescription(req.description());
        return Mappers.toSymptomResponse(symptomRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        if (!symptomRepository.existsById(id)) throw new NotFoundException("Симптом не найден");
        symptomRepository.deleteById(id);
    }
}
