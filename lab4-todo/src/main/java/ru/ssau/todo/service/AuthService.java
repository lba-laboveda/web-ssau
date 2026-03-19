package ru.ssau.todo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ru.ssau.todo.dto.AuthUserDto;
import ru.ssau.todo.dto.TokenResponse;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService    = tokenService;
    }

    public TokenResponse login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;  // null = неверные учётные данные, контроллер вернёт 401
        }

        List<String> roles = extractRoles(user);
        String accessToken  = tokenService.generateToken(tokenService.createAccessPayload(user.getId(), roles));
        String refreshToken = tokenService.generateToken(tokenService.createRefreshPayload(user.getId()));
        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refresh(String refreshToken) {
        // validateToken бросит TokenExpiredException / InvalidTokenException при ошибке
        var payload = tokenService.validateToken(refreshToken);

        // Refresh Token не должен содержать roles — это признак Access Token
        if (payload.containsKey("roles")) {
            return null;
        }

        Long userId = ((Number) payload.get("userId")).longValue();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        List<String> roles = extractRoles(user);
        String newAccessToken = tokenService.generateToken(
                tokenService.createAccessPayload(user.getId(), roles));
        return new TokenResponse(newAccessToken, refreshToken);
    }

    public AuthUserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;

        AuthUserDto dto = new AuthUserDto();
        dto.setUsername(user.getUsername());
        dto.setRoles(extractRoles(user));
        return dto;
    }

    // Вынесено в отдельный метод — было продублировано в login и refresh
    private List<String> extractRoles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}