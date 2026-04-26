package com.medcenter.controller;

import com.medcenter.dto.AppointmentDtos;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    public AppointmentController(AppointmentService appointmentService, AuthenticatedUser currentUser) {
        this.appointmentService = appointmentService;
        this.currentUser = currentUser;
    }


    private final AppointmentService appointmentService;
    private final AuthenticatedUser currentUser;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDtos.AppointmentResponse> create(@Valid @RequestBody AppointmentDtos.CreateAppointmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(appointmentService.createForPatient(currentUser.current().getUsername(), req));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AppointmentDtos.AppointmentResponse> listAll(Pageable pageable) {
        return appointmentService.listAll(pageable);
    }

    @GetMapping("/me")
    public Page<AppointmentDtos.AppointmentResponse> listMine(Pageable pageable) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isDoctor = hasAuthority(auth.getAuthorities(), "ROLE_DOCTOR");
        if (isDoctor) {
            return appointmentService.listForDoctor(currentUser.current().getUsername(), pageable);
        }
        return appointmentService.listForPatient(currentUser.current().getUsername(), pageable);
    }

    @GetMapping("/me/upcoming")
    public List<AppointmentDtos.AppointmentResponse> upcoming() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isDoctor = hasAuthority(auth.getAuthorities(), "ROLE_DOCTOR");
        if (isDoctor) {
            return appointmentService.upcomingForDoctor(currentUser.current().getUsername());
        }
        return appointmentService.upcomingForPatient(currentUser.current().getUsername());
    }

    @GetMapping("/{id}")
    public AppointmentDtos.AppointmentResponse get(@PathVariable Long id) {
        return appointmentService.get(id);
    }

    @PatchMapping("/{id}/status")
    public AppointmentDtos.AppointmentResponse changeStatus(@PathVariable Long id, @Valid @RequestBody AppointmentDtos.UpdateStatusRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = hasAuthority(auth.getAuthorities(), "ROLE_ADMIN");
        return appointmentService.changeStatus(id, req.status(), currentUser.current().getUsername(), isAdmin);
    }

    private static boolean hasAuthority(java.util.Collection<? extends GrantedAuthority> auths, String name) {
        return auths != null && auths.stream().anyMatch(a -> a.getAuthority().equals(name));
    }
}
