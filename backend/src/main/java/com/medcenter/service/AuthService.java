package com.medcenter.service;

import com.medcenter.domain.*;
import com.medcenter.domain.enums.RoleName;
import com.medcenter.dto.AuthDtos;
import com.medcenter.exception.ConflictException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.repository.*;
import com.medcenter.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PatientRepository patientRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Самостоятельная регистрация — заводит только пациента (см. ТЗ).
     * Врачей и администраторов добавляет администратор из админки.
     */
    @Transactional
    public AuthDtos.JwtResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        Role role = roleRepository.findByName(RoleName.ROLE_PATIENT)
            .orElseThrow(() -> new NotFoundException("Роль PATIENT не найдена"));

        User user = User.builder()
            .username(req.username())
            .email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .fullName(req.fullName())
            .phone(req.phone())
            .enabled(true)
            .build();
        user.getRoles().add(role);
        user = userRepository.save(user);

        Patient patient = Patient.builder()
            .user(user)
            .birthDate(req.birthDate())
            .address(req.address())
            .insuranceNumber(req.insuranceNumber())
            .build();
        patientRepository.save(patient);

        return buildJwtResponse(user);
    }

    public AuthDtos.JwtResponse login(AuthDtos.LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        User user = userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return buildJwtResponse(user);
    }

    private AuthDtos.JwtResponse buildJwtResponse(User user) {
        Set<String> roles = user.getRoles().stream()
            .map(r -> r.getName().name())
            .collect(Collectors.toSet());
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("roles", roles);
        String token = jwtService.generateToken(user.getUsername(), claims);
        return new AuthDtos.JwtResponse(token, "Bearer", user.getId(), user.getUsername(), user.getFullName(), roles);
    }
}
