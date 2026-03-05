// ru.ssau.todo.config.JwtConfig.java
package ru.ssau.todo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.access-expiration:900}") // 15 минут в секундах
    private long accessExpiration;

    @Value("${jwt.refresh-expiration:604800}") // 7 дней в секундах
    private long refreshExpiration;

    public String getSecret() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
        return secret;
    }

    public long getAccessExpiration() {
        return accessExpiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}