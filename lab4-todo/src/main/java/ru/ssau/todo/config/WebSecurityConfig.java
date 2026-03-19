package ru.ssau.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ru.ssau.todo.filter.JwtFilter;

@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public WebSecurityConfig(JwtFilter jwtFilter,
                             UserDetailsService userDetailsService,
                             PasswordEncoder passwordEncoder) {
        this.jwtFilter          = jwtFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder    = passwordEncoder;
    }

    // Spring Security 6: UserDetailsService передаётся в конструктор, не через сеттер
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(daoAuthenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты — не требуют входа
                        .requestMatchers("/users/register", "/auth/login", "/auth/refresh").permitAll()
                        // DELETE /tasks/** — только администратор
                        .requestMatchers(HttpMethod.DELETE, "/tasks/**").hasRole("ADMIN")
                        // Все остальные — только авторизованные пользователи
                        .anyRequest().authenticated()
                )
                // Наш JWT-фильтр стоит перед стандартной аутентификацией
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Basic Auth и Form Login отключены — используем токены
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}