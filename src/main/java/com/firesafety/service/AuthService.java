package com.firesafety.service;

import com.firesafety.dto.request.LoginRequest;
import com.firesafety.dto.request.RefreshTokenRequest;
import com.firesafety.dto.response.AuthResponse;
import com.firesafety.entity.User;
import com.firesafety.repository.UserRepository;
import com.firesafety.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // Отвечает за вход пользователя: проверяет логин/пароль и выдаёт JWT.
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final Set<String> revokedRefreshTokens = ConcurrentHashMap.newKeySet();

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Spring Security сам проверяет пароль и статус пользователя.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // После успешной проверки создаём токен для доступа к защищённым endpoint'ам.
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        log.info("User {} logged in successfully", request.getUsername());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (revokedRefreshTokens.contains(refreshToken) || !tokenProvider.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        revokedRefreshTokens.add(refreshToken);
        return buildAuthResponse(
                user,
                tokenProvider.generateAccessToken(username),
                tokenProvider.generateRefreshToken(username)
        );
    }

    public void logout(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (tokenProvider.validateRefreshToken(refreshToken)) {
            revokedRefreshTokens.add(refreshToken);
        }
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
    }
}
