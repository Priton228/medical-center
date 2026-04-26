package com.medcenter.service;

import com.medcenter.domain.Doctor;
import com.medcenter.domain.Patient;
import com.medcenter.domain.Role;
import com.medcenter.domain.User;
import com.medcenter.domain.enums.RoleName;
import com.medcenter.dto.UserDtos;
import com.medcenter.exception.BadRequestException;
import com.medcenter.exception.ConflictException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.DoctorRepository;
import com.medcenter.repository.PatientRepository;
import com.medcenter.repository.RoleRepository;
import com.medcenter.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PatientRepository patientRepository,
                       DoctorRepository doctorRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDtos.UserResponse> list(Pageable pageable) {
        return userRepository.findAllByOrderByIdAsc(pageable).map(Mappers::toUserResponse);
    }

    @Transactional(readOnly = true)
    public UserDtos.UserResponse get(Long id) {
        return userRepository.findById(id).map(Mappers::toUserResponse)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Transactional
    public UserDtos.UserResponse updateProfile(User user, UserDtos.UpdateProfileRequest req) {
        if (!user.getEmail().equalsIgnoreCase(req.email()) && userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email уже используется");
        }
        user.setEmail(req.email());
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        return Mappers.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(User user, UserDtos.ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Старый пароль неверен");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserDtos.UserResponse setEnabled(Long id, boolean enabled) {
        User u = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        u.setEnabled(enabled);
        return Mappers.toUserResponse(userRepository.save(u));
    }

    /**
     * Создание пользователя администратором с произвольным набором ролей.
     * Если в ролях есть PATIENT/DOCTOR — автоматически создаётся соответствующая запись.
     */
    @Transactional
    public UserDtos.UserResponse adminCreate(UserDtos.AdminCreateUserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Имя пользователя занято");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email занят");
        }
        if (req.roles() == null || req.roles().isEmpty()) {
            throw new BadRequestException("Не указаны роли");
        }
        User user = User.builder()
            .username(req.username())
            .email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .fullName(req.fullName())
            .phone(req.phone())
            .enabled(true)
            .build();
        user.setRoles(resolveRoles(req.roles()));
        user = userRepository.save(user);
        ensureProfileForRoles(user, req.roles());
        return Mappers.toUserResponse(user);
    }

    /** Полное обновление профиля и ролей любого пользователя администратором. */
    @Transactional
    public UserDtos.UserResponse adminUpdate(Long id, UserDtos.AdminUpdateUserRequest req) {
        User u = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (!u.getEmail().equalsIgnoreCase(req.email()) && userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email уже используется");
        }
        u.setEmail(req.email());
        u.setFullName(req.fullName());
        u.setPhone(req.phone());
        if (req.enabled() != null) u.setEnabled(req.enabled());
        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.newPassword()));
        }
        if (req.roles() != null && !req.roles().isEmpty()) {
            u.setRoles(resolveRoles(req.roles()));
            ensureProfileForRoles(u, req.roles());
        }
        return Mappers.toUserResponse(userRepository.save(u));
    }

    /** Изменение набора ролей: добавляет/удаляет связанные профили (Patient/Doctor). */
    @Transactional
    public UserDtos.UserResponse setRoles(Long id, Set<RoleName> roles) {
        User u = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (roles == null || roles.isEmpty()) {
            throw new BadRequestException("Не указаны роли");
        }
        u.setRoles(resolveRoles(roles));
        ensureProfileForRoles(u, roles);
        return Mappers.toUserResponse(userRepository.save(u));
    }

    @Transactional
    public void delete(Long id) {
        User u = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        userRepository.delete(u);
    }

    private Set<Role> resolveRoles(Set<RoleName> names) {
        return names.stream()
            .map(n -> roleRepository.findByName(n)
                .orElseThrow(() -> new NotFoundException("Роль " + n + " не найдена")))
            .collect(Collectors.toCollection(HashSet::new));
    }

    private void ensureProfileForRoles(User user, Set<RoleName> roles) {
        if (roles.contains(RoleName.ROLE_PATIENT)
            && patientRepository.findByUserUsername(user.getUsername()).isEmpty()) {
            patientRepository.save(Patient.builder().user(user).build());
        }
        if (roles.contains(RoleName.ROLE_DOCTOR)
            && doctorRepository.findByUserUsername(user.getUsername()).isEmpty()) {
            doctorRepository.save(Doctor.builder()
                .user(user)
                .specialization("Терапевт")
                .workStart(LocalTime.of(9, 0))
                .workEnd(LocalTime.of(18, 0))
                .available(true)
                .build());
        }
    }
}
