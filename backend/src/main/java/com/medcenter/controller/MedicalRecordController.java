package com.medcenter.controller;

import com.medcenter.dto.MedicalRecordDtos;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
public class MedicalRecordController {
    public MedicalRecordController(MedicalRecordService service, AuthenticatedUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }


    private final MedicalRecordService service;
    private final AuthenticatedUser currentUser;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<MedicalRecordDtos.MedicalRecordResponse> mine() {
        return service.forCurrentPatient(currentUser.current().getUsername());
    }

    @GetMapping("/by-doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<MedicalRecordDtos.MedicalRecordResponse> byDoctor() {
        return service.byDoctor(currentUser.current().getUsername());
    }

    @GetMapping("/patient/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<MedicalRecordDtos.MedicalRecordResponse> ofPatient(@PathVariable Long id) {
        return service.forPatient(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordDtos.MedicalRecordResponse> create(@Valid @RequestBody MedicalRecordDtos.MedicalRecordRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.createByDoctor(currentUser.current().getUsername(), req));
    }
}
