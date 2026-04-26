package com.medcenter.controller;

import com.medcenter.dto.DiagnosisDtos;
import com.medcenter.service.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diagnoses")
public class DiagnosisController {
    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }


    private final DiagnosisService diagnosisService;

    @GetMapping
    public List<DiagnosisDtos.DiagnosisResponse> list() {
        return diagnosisService.listAll();
    }

    @GetMapping("/{id}")
    public DiagnosisDtos.DiagnosisResponse get(@PathVariable Long id) {
        return diagnosisService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiagnosisDtos.DiagnosisResponse> create(@Valid @RequestBody DiagnosisDtos.DiagnosisRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diagnosisService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosisDtos.DiagnosisResponse update(@PathVariable Long id, @Valid @RequestBody DiagnosisDtos.DiagnosisRequest req) {
        return diagnosisService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diagnosisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
