package com.medcenter.controller;

import com.medcenter.dto.StatsDtos;
import com.medcenter.dto.UserDtos;
import com.medcenter.service.StatsService;
import com.medcenter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    public AdminController(StatsService statsService, UserService userService) {
        this.statsService = statsService;
        this.userService = userService;
    }

    private final StatsService statsService;
    private final UserService userService;

    @GetMapping("/stats/overview")
    public StatsDtos.OverviewStats overview() {
        return statsService.overview();
    }

    @PostMapping("/users")
    public ResponseEntity<UserDtos.UserResponse> createUser(
        @Valid @RequestBody UserDtos.AdminCreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.adminCreate(req));
    }

    @PutMapping("/users/{id}")
    public UserDtos.UserResponse updateUser(@PathVariable Long id,
                                            @Valid @RequestBody UserDtos.AdminUpdateUserRequest req) {
        return userService.adminUpdate(id, req);
    }

    @PatchMapping("/users/{id}/roles")
    public UserDtos.UserResponse setRoles(@PathVariable Long id,
                                          @Valid @RequestBody UserDtos.SetRolesRequest req) {
        return userService.setRoles(id, req.roles());
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
