// ru.ssau.todo.controller.AuthController.java
package ru.ssau.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.ssau.todo.dto.*;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.service.TokenService;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        var roles = user.getRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toList());

        try {
            String accessToken = tokenService.generateToken(
                    tokenService.createAccessPayload(user.getId(), roles));
            String refreshToken = tokenService.generateToken(
                    tokenService.createRefreshPayload(user.getId()));

            return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            Map<String, Object> payload = tokenService.validateToken(request.getRefreshToken());

            if (payload.containsKey("roles")) {
                return ResponseEntity.status(401).build();
            }

            Long userId = ((Number) payload.get("userId")).longValue();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var roles = user.getRoles().stream()
                    .map(role -> role.getName().replace("ROLE_", ""))
                    .collect(Collectors.toList());

            String newAccessToken = tokenService.generateToken(
                    tokenService.createAccessPayload(user.getId(), roles));

            return ResponseEntity.ok(new TokenResponse(newAccessToken, request.getRefreshToken()));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserDto> getCurrentUser() {
        // ✅ ИСПРАВЛЕНО: добавлены импорты
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var roles = user.getRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toList());

        // ✅ ИСПРАВЛЕНО: используем сеттеры вместо конструктора
        AuthUserDto dto = new AuthUserDto();
        dto.setUsername(user.getUsername());
        dto.setRoles(roles);

        return ResponseEntity.ok(dto);
    }
}