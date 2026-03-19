package ru.ssau.todo.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.exception.TokenException;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.service.TokenService;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String[] EXCLUDED_PATHS = {
            "/auth/login",
            "/auth/refresh",
            "/users/register"
    };

    private final TokenService   tokenService;
    private final UserRepository userRepository;

    public JwtFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService   = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Map<String, Object> payload = tokenService.validateToken(token);
            // validateToken бросает TokenExpiredException или InvalidTokenException —
            // теперь понятно по названию что именно пошло не так

            Long userId = ((Number) payload.get("userId")).longValue();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) payload.get("roles");

            Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();
            if (roles != null) {
                authorities = new ArrayList<>();
                for (String role : roles) {
                    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(roleName));
                }
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities)
            );

        } catch (TokenException e) {
            // Специфичные исключения токена — точно знаем что это проблема токена
            sendUnauthorized(response, e.getMessage());
            return;
        } catch (Exception e) {
            // Любая другая ошибка (например пользователь не найден в БД) — тоже 401
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}