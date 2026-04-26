package com.medcenter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDtos {

    public record UserResponse(
        Long id,
        String login,
        String email,
        String fullName,
        String phone,
        String avatarUrl,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt
    ) {}

    public record UpdateProfileRequest(
        @NotBlank @Email String email,
        @NotBlank String fullName,
        String phone
    ) {}

    public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
    ) {}

    public record ToggleEnabledRequest(boolean enabled) {}

    public record AvatarResponse(String avatarUrl) {}
}
