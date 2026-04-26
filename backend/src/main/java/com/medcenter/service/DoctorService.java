package com.medcenter.service;

import com.medcenter.domain.Doctor;
import com.medcenter.domain.Role;
import com.medcenter.domain.User;
import com.medcenter.domain.enums.RoleName;
import com.medcenter.dto.DoctorDtos;
import com.medcenter.exception.ConflictException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.DoctorRepository;
import com.medcenter.repository.RoleRepository;
import com.medcenter.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorService {
    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<DoctorDtos.DoctorResponse> list(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(Mappers::toDoctorResponse);
    }

    @Transactional(readOnly = true)
    public List<DoctorDtos.DoctorResponse> findBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationIgnoreCase(specialization).stream()
            .map(Mappers::toDoctorResponse).toList();
    }

    @Transactional(readOnly = true)
    public DoctorDtos.DoctorResponse get(Long id) {
        return doctorRepository.findById(id).map(Mappers::toDoctorResponse)
            .orElseThrow(() -> new NotFoundException("Врач не найден"));
    }

    @Transactional(readOnly = true)
    public DoctorDtos.DoctorResponse getByUsername(String username) {
        return doctorRepository.findByUserUsername(username).map(Mappers::toDoctorResponse)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));
    }

    @Transactional
    public DoctorDtos.DoctorResponse create(DoctorDtos.CreateDoctorRequest req) {
        if (userRepository.existsByUsername(req.username())) throw new ConflictException("Имя пользователя занято");
        if (userRepository.existsByEmail(req.email())) throw new ConflictException("Email занят");
        Role role = roleRepository.findByName(RoleName.ROLE_DOCTOR)
            .orElseThrow(() -> new NotFoundException("Роль врача не найдена"));
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
        java.time.LocalTime workStart = req.workStart() != null ? req.workStart() : java.time.LocalTime.of(9, 0);
        java.time.LocalTime workEnd = req.workEnd() != null ? req.workEnd() : java.time.LocalTime.of(18, 0);
        Doctor doctor = Doctor.builder()
            .user(user)
            .specialization(req.specialization())
            .bio(req.bio())
            .photoUrl(req.photoUrl())
            .roomNumber(req.roomNumber())
            .workStart(workStart)
            .workEnd(workEnd)
            .available(req.available() == null ? true : req.available())
            .build();
        return Mappers.toDoctorResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorDtos.DoctorResponse update(Long id, DoctorDtos.UpdateDoctorRequest req) {
        Doctor d = doctorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Врач не найден"));
        d.setSpecialization(req.specialization());
        d.setBio(req.bio());
        d.setPhotoUrl(req.photoUrl());
        d.setAvailable(Boolean.TRUE.equals(req.available()));
        d.setWorkStart(req.workStart());
        d.setWorkEnd(req.workEnd());
        d.setRoomNumber(req.roomNumber());
        return Mappers.toDoctorResponse(doctorRepository.save(d));
    }

    @Transactional
    public DoctorDtos.DoctorResponse updateOwnSchedule(String username, DoctorDtos.UpdateScheduleRequest req) {
        Doctor d = doctorRepository.findByUserUsername(username)
            .orElseThrow(() -> new NotFoundException("Профиль врача не найден"));
        d.setWorkStart(req.workStart());
        d.setWorkEnd(req.workEnd());
        d.setAvailable(Boolean.TRUE.equals(req.available()));
        return Mappers.toDoctorResponse(doctorRepository.save(d));
    }

    @Transactional
    public void delete(Long id) {
        Doctor d = doctorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Врач не найден"));
        Long userId = d.getUser().getId();
        doctorRepository.delete(d);
        userRepository.deleteById(userId);
    }
}
