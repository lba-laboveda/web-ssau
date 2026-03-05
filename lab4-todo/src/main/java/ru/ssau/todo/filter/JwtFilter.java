// ru.ssau.todo.filter.JwtFilter.java
package ru.ssau.todo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.ssau.todo.service.TokenService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String[] EXCLUDED_PATHS = {
            "/auth/login",
            "/auth/refresh",
            "/users/register"
    };

    private final TokenService tokenService;

    public JwtFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                return true;
            }
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
            // Валидация токена
            Map<String, Object> payload = tokenService.validateToken(token);

            // Извлечение данных
            Long userId = ((Number) payload.get("userId")).longValue();

            // ✅ ИСПРАВЛЕНО: извлекаем как Collection<String>
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) payload.get("roles");

            // ✅ ИСПРАВЛЕНО: создаём authorities правильно
            Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();
            if (roles != null) {
                authorities = new ArrayList<>();
                for (String role : roles) {
                    // Убираем префикс ROLE_ если он уже есть, чтобы не было ROLE_ROLE_USER
                    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(roleName));
                }
            }

            // Установка в SecurityContext
            var authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities // ✅ Теперь тип корректный: Collection<? extends GrantedAuthority>
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Токен невалиден — очищаем контекст и возвращаем 401
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}