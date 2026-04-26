package com.medcenter.service;

import com.medcenter.domain.Patient;
import com.medcenter.domain.Role;
import com.medcenter.domain.User;
import com.medcenter.domain.enums.RoleName;
import com.medcenter.dto.PatientDtos;
import com.medcenter.exception.ConflictException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.PatientRepository;
import com.medcenter.repository.RoleRepository;
import com.medcenter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<PatientDtos.PatientResponse> list(Pageable pageable) {
        return patientRepository.findAll(pageable).map(Mappers::toPatientResponse);
    }

    @Transactional(readOnly = true)
    public PatientDtos.PatientResponse get(Long id) {
        return patientRepository.findById(id).map(Mappers::toPatientResponse)
            .orElseThrow(() -> new NotFoundException("Пациент не найден"));
    }

    @Transactional(readOnly = true)
    public PatientDtos.PatientResponse getByUsername(String username) {
        return patientRepository.findByUserUsername(username).map(Mappers::toPatientResponse)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
    }

    @Transactional
    public PatientDtos.PatientResponse updateOwn(String username, PatientDtos.UpdatePatientRequest req) {
        Patient p = patientRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль пациента не найден"));
        p.setBirthDate(req.birthDate());
        p.setAddress(req.address());
        p.setInsuranceNumber(req.insuranceNumber());
        return Mappers.toPatientResponse(patientRepository.save(p));
    }

    @Transactional
    public PatientDtos.PatientResponse update(Long id, PatientDtos.UpdatePatientRequest req) {
        Patient p = patientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Пациент не найден"));
        p.setBirthDate(req.birthDate());
        p.setAddress(req.address());
        p.setInsuranceNumber(req.insuranceNumber());
        return Mappers.toPatientResponse(patientRepository.save(p));
    }

    @Transactional
    public PatientDtos.PatientResponse create(PatientDtos.CreatePatientRequest req) {
        if (userRepository.existsByUsername(req.username())) throw new ConflictException("Имя пользователя занято");
        if (userRepository.existsByEmail(req.email())) throw new ConflictException("Email занят");
        Role role = roleRepository.findByName(RoleName.ROLE_PATIENT)
            .orElseThrow(() -> new NotFoundException("Роль пациента не найдена"));
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
        Patient p = Patient.builder()
            .user(user)
            .birthDate(req.birthDate())
            .address(req.address())
            .insuranceNumber(req.insuranceNumber())
            .build();
        return Mappers.toPatientResponse(patientRepository.save(p));
    }
}
