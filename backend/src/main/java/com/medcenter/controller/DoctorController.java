package com.medcenter.controller;

import com.medcenter.dto.DoctorDtos;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AuthenticatedUser currentUser;

    @GetMapping
    public Page<DoctorDtos.DoctorResponse> list(
        Pageable pageable,
        @RequestParam(required = false) String specialization
    ) {
        if (specialization != null && !specialization.isBlank()) {
            List<DoctorDtos.DoctorResponse> filtered = doctorService.findBySpecialization(specialization);
            return new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }
        return doctorService.list(pageable);
    }

    @GetMapping("/{id}")
    public DoctorDtos.DoctorResponse get(@PathVariable Long id) {
        return doctorService.get(id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorDtos.DoctorResponse me() {
        return doctorService.getByUsername(currentUser.current().getLogin());
    }

    @PutMapping("/me/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorDtos.DoctorResponse updateMySchedule(@Valid @RequestBody DoctorDtos.UpdateScheduleRequest req) {
        return doctorService.updateOwnSchedule(currentUser.current().getLogin(), req);
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorDtos.DoctorResponse updateMyProfile(@Valid @RequestBody DoctorDtos.UpdateOwnProfileRequest req) {
        return doctorService.updateOwnProfile(currentUser.current().getLogin(), req);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorDtos.DoctorResponse> create(@Valid @RequestBody DoctorDtos.CreateDoctorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorDtos.DoctorResponse update(@PathVariable Long id, @Valid @RequestBody DoctorDtos.UpdateDoctorRequest req) {
        return doctorService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
