package com.firesafety.controller;

import com.firesafety.dto.request.LoginRequest;
import com.firesafety.dto.response.AuthResponse;
import com.firesafety.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    // Сервис проверяет логин/пароль и выдаёт JWT-токен.
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // POST /api/auth/login используется перед защищёнными запросами в Swagger.
        return ResponseEntity.ok(authService.login(request));
    }
}
