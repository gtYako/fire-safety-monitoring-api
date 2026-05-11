package com.firesafety.service;

import com.firesafety.dto.request.LoginRequest;
import com.firesafety.dto.response.AuthResponse;
import com.firesafety.entity.Role;
import com.firesafety.entity.User;
import com.firesafety.repository.UserRepository;
import com.firesafety.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("test-jwt-token");

        Role role = Role.builder().name("ADMIN").build();
        User user = User.builder()
                .id(1L)
                .username("admin")
                .roles(Set.of(role))
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRoles()).contains("ADMIN");
        verify(authenticationManager, times(1)).authenticate(any());
    }
}
