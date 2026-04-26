package com.medcenter.controller;

import com.medcenter.dto.AppointmentDtos;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthenticatedUser currentUser;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDtos.AppointmentResponse> create(@Valid @RequestBody AppointmentDtos.CreateAppointmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(appointmentService.createForPatient(currentUser.current().getLogin(), req));
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
            return appointmentService.listForDoctor(currentUser.current().getLogin(), pageable);
        }
        return appointmentService.listForPatient(currentUser.current().getLogin(), pageable);
    }

    @GetMapping("/me/upcoming")
    public List<AppointmentDtos.AppointmentResponse> upcoming() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isDoctor = hasAuthority(auth.getAuthorities(), "ROLE_DOCTOR");
        if (isDoctor) {
            return appointmentService.upcomingForDoctor(currentUser.current().getLogin());
        }
        return appointmentService.upcomingForPatient(currentUser.current().getLogin());
    }

    @GetMapping("/{id}")
    public AppointmentDtos.AppointmentResponse get(@PathVariable Long id) {
        return appointmentService.get(id);
    }

    @PatchMapping("/{id}/status")
    public AppointmentDtos.AppointmentResponse changeStatus(@PathVariable Long id, @Valid @RequestBody AppointmentDtos.UpdateStatusRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = hasAuthority(auth.getAuthorities(), "ROLE_ADMIN");
        return appointmentService.changeStatus(id, req.status(), currentUser.current().getLogin(), isAdmin);
    }

    /** Перенос приёма на новое время — пациентом, врачом или админом. */
    @PatchMapping("/{id}/reschedule")
    public AppointmentDtos.AppointmentResponse reschedule(@PathVariable Long id,
                                                          @Valid @RequestBody AppointmentDtos.RescheduleRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = hasAuthority(auth.getAuthorities(), "ROLE_ADMIN");
        return appointmentService.reschedule(id, req, currentUser.current().getLogin(), isAdmin);
    }

    /** Подтверждение записи по одноразовой ссылке из письма (без авторизации). */
    @GetMapping("/confirm")
    public AppointmentDtos.AppointmentResponse confirmByToken(@RequestParam("token") String token) {
        return appointmentService.confirmByToken(token);
    }

    /** Отмена записи по одноразовой ссылке из письма (без авторизации). */
    @GetMapping("/reject")
    public AppointmentDtos.AppointmentResponse rejectByToken(@RequestParam("token") String token) {
        return appointmentService.rejectByToken(token);
    }

    private static boolean hasAuthority(java.util.Collection<? extends GrantedAuthority> auths, String name) {
        return auths != null && auths.stream().anyMatch(a -> a.getAuthority().equals(name));
    }
}
