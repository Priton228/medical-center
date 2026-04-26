package com.medcenter.service;

import com.medcenter.domain.User;
import com.medcenter.dto.UserDtos;
import com.medcenter.exception.BadRequestException;
import com.medcenter.exception.ConflictException;
import com.medcenter.exception.NotFoundException;
import com.medcenter.mapper.Mappers;
import com.medcenter.repository.UserRepository;
import com.medcenter.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorage;

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

    /** Загружает аватар текущего пользователя и сохраняет его публичный URL. */
    @Transactional
    public UserDtos.UserResponse uploadAvatar(User user, MultipartFile file) {
        String oldUrl = user.getAvatarUrl();
        String url = fileStorage.saveAvatar(user.getId(), file);
        user.setAvatarUrl(url);
        User saved = userRepository.save(user);
        if (oldUrl != null && !oldUrl.equals(url)) {
            fileStorage.deleteByPublicUrl(oldUrl);
        }
        return Mappers.toUserResponse(saved);
    }

    /** Удаляет аватар текущего пользователя. */
    @Transactional
    public UserDtos.UserResponse removeAvatar(User user) {
        String old = user.getAvatarUrl();
        user.setAvatarUrl(null);
        User saved = userRepository.save(user);
        if (old != null) fileStorage.deleteByPublicUrl(old);
        return Mappers.toUserResponse(saved);
    }
}
