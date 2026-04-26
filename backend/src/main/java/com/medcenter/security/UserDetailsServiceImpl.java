package com.medcenter.security;

import com.medcenter.domain.User;
import com.medcenter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Внутри Spring Security имя «username» = наш {@code login}.
 * Загружаем пользователя по логину.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
            .map(r -> new SimpleGrantedAuthority(r.getName().name()))
            .collect(Collectors.toSet());
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getLogin())
            .password(user.getPassword())
            .disabled(!user.isEnabled())
            .authorities(authorities)
            .build();
    }
}
