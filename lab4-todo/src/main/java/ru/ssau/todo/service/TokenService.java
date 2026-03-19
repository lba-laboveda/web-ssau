package ru.ssau.todo.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.ssau.todo.exception.InvalidTokenException;
import ru.ssau.todo.exception.TokenExpiredException;

@Service
public class TokenService {

    private static final String HMAC_SHA256      = "HmacSHA256";
    private static final long   ACCESS_EXPIRATION  = 15 * 60;        // 15 минут
    private static final long   REFRESH_EXPIRATION = 7 * 24 * 3600;  // 7 дней

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Секрет читается из переменной среды — не из кода и не из конфига
    private String getSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable is not set or shorter than 32 characters");
        }
        return secret;
    }

    public String generateToken(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(getSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] signatureBytes = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));

            String encodedSignature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signatureBytes);

            return encodedPayload + "." + encodedSignature;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> validateToken(String token) {
        // Проверка структуры — ровно две части через точку
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new InvalidTokenException("expected 2 parts, got " + parts.length);
        }

        String encodedPayload    = parts[0];
        String providedSignature = parts[1];

        // Пересчитываем подпись и сравниваем
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(getSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] expectedBytes = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(expectedBytes);

            if (!constantTimeEquals(providedSignature, expectedSignature)) {
                throw new InvalidTokenException("signature mismatch");
            }

            byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
            Map<String, Object> payload = objectMapper.readValue(decoded, Map.class);

            // Проверка срока действия
            Number exp = (Number) payload.get("exp");
            if (exp == null || Instant.now().isAfter(Instant.ofEpochSecond(exp.longValue()))) {
                throw new TokenExpiredException();
            }

            return payload;
        } catch (InvalidTokenException | TokenExpiredException e) {
            throw e;  // специфичные — пробрасываем как есть
        } catch (Exception e) {
            throw new InvalidTokenException("malformed payload");
        }
    }

    // Защита от timing-атак: сравнение за константное время
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public Map<String, Object> createAccessPayload(Long userId, List<String> roles) {
        long now = Instant.now().getEpochSecond();
        return Map.of(
                "userId", userId,
                "roles",  roles,
                "iat",    now,
                "exp",    now + ACCESS_EXPIRATION
        );
    }

    public Map<String, Object> createRefreshPayload(Long userId) {
        long now = Instant.now().getEpochSecond();
        return Map.of(
                "userId", userId,
                "iat",    now,
                "exp",    now + REFRESH_EXPIRATION
        );
    }

    public Long extractUserId(String token) {
        return ((Number) validateToken(token).get("userId")).longValue();
    }
}