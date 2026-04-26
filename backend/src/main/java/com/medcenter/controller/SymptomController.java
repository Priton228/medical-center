package com.medcenter.controller;

import com.medcenter.dto.SymptomDtos;
import com.medcenter.service.SymptomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/symptoms")
public class SymptomController {
    public SymptomController(SymptomService symptomService) {
        this.symptomService = symptomService;
    }


    private final SymptomService symptomService;

    @GetMapping
    public List<SymptomDtos.SymptomResponse> list(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) return symptomService.search(q);
        return symptomService.listAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SymptomDtos.SymptomResponse> create(@Valid @RequestBody SymptomDtos.SymptomRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(symptomService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SymptomDtos.SymptomResponse update(@PathVariable Long id, @Valid @RequestBody SymptomDtos.SymptomRequest req) {
        return symptomService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        symptomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
