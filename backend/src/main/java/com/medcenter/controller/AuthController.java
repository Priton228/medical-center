package com.medcenter.controller;

import com.medcenter.dto.AuthDtos;
import com.medcenter.dto.UserDtos;
import com.medcenter.mapper.Mappers;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticatedUser currentUser;

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.JwtResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public AuthDtos.JwtResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserDtos.UserResponse me() {
        return Mappers.toUserResponse(currentUser.current());
    }
}
