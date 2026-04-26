package com.medcenter.security;

import com.medcenter.domain.User;
import com.medcenter.exception.ForbiddenException;
import com.medcenter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUser {

    private final UserRepository userRepository;

    public User current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Не аутентифицирован");
        }
        String login = auth.getName();
        return userRepository.findByLogin(login)
            .orElseThrow(() -> new ForbiddenException("Пользователь не найден: " + login));
    }
}
