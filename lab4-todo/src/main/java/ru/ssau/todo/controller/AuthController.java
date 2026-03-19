package ru.ssau.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.ssau.todo.dto.AuthUserDto;
import ru.ssau.todo.dto.LoginRequest;
import ru.ssau.todo.dto.RefreshRequest;
import ru.ssau.todo.dto.TokenResponse;
import ru.ssau.todo.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request.getUsername(), request.getPassword());
        return tokens != null
                ? ResponseEntity.ok(tokens)
                : ResponseEntity.status(401).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            TokenResponse tokens = authService.refresh(request.getRefreshToken());
            return tokens != null
                    ? ResponseEntity.ok(tokens)
                    : ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        AuthUserDto dto = authService.getCurrentUser(auth.getName());
        return dto != null
                ? ResponseEntity.ok(dto)
                : ResponseEntity.status(401).build();
    }
}