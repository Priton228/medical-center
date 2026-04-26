package com.medcenter.controller;

import com.medcenter.dto.UserDtos;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticatedUser currentUser;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDtos.UserResponse> list(Pageable pageable) {
        return userService.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public UserDtos.UserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PutMapping("/me")
    public UserDtos.UserResponse updateMe(@Valid @RequestBody UserDtos.UpdateProfileRequest req) {
        return userService.updateProfile(currentUser.current(), req);
    }

    @PostMapping("/me/password")
    public void changePassword(@Valid @RequestBody UserDtos.ChangePasswordRequest req) {
        userService.changePassword(currentUser.current(), req);
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDtos.UserResponse setEnabled(@PathVariable Long id, @RequestBody UserDtos.ToggleEnabledRequest req) {
        return userService.setEnabled(id, req.enabled());
    }

    /** Загрузка аватара текущего пользователя (multipart). */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDtos.UserResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(currentUser.current(), file);
    }

    /** Сброс аватара. */
    @DeleteMapping("/me/avatar")
    public UserDtos.UserResponse removeAvatar() {
        return userService.removeAvatar(currentUser.current());
    }

    /** Возвращает данные текущего пользователя. */
    @GetMapping("/me")
    public UserDtos.UserResponse me() {
        return userService.get(currentUser.current().getId());
    }
}
