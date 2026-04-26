package com.medcenter.dto;

import com.medcenter.domain.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDtos {

    public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
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

    /** Создание пользователя администратором — с произвольным набором ролей. */
    public record AdminCreateUserRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 64) String password,
        @NotBlank @Size(max = 128) String fullName,
        String phone,
        @NotEmpty Set<RoleName> roles
    ) {}

    /** Полное обновление профиля любого пользователя администратором. */
    public record AdminUpdateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 128) String fullName,
        String phone,
        Boolean enabled,
        Set<RoleName> roles,
        String newPassword
    ) {}

    public record SetRolesRequest(
        @NotEmpty Set<RoleName> roles
    ) {}
}
