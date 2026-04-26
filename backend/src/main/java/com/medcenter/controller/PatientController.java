package com.medcenter.controller;

import com.medcenter.dto.PatientDtos;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AuthenticatedUser currentUser;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public Page<PatientDtos.PatientResponse> list(Pageable pageable) {
        return patientService.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public PatientDtos.PatientResponse get(@PathVariable Long id) {
        return patientService.get(id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientDtos.PatientResponse me() {
        return patientService.getByUsername(currentUser.current().getLogin());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientDtos.PatientResponse updateMe(@Valid @RequestBody PatientDtos.UpdatePatientRequest req) {
        return patientService.updateOwn(currentUser.current().getLogin(), req);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientDtos.PatientResponse> create(@Valid @RequestBody PatientDtos.CreatePatientRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientDtos.PatientResponse update(@PathVariable Long id, @Valid @RequestBody PatientDtos.UpdatePatientRequest req) {
        return patientService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
