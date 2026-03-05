// ru.ssau.todo.service.TokenService.java
package ru.ssau.todo.service;

import com.fasterxml.jackson.databind.ObjectMapper; // ✅ Теперь работает
import org.springframework.stereotype.Service;
import ru.ssau.todo.config.JwtConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class TokenService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final JwtConfig jwtConfig;

    public TokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(Map<String, Object> payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);

        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));

        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec key = new SecretKeySpec(
                jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256
        );
        mac.init(key);
        byte[] signatureBytes = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));

        String encodedSignature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(signatureBytes);

        return encodedPayload + "." + encodedSignature;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> validateToken(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new Exception("Invalid token structure");
        }

        String encodedPayload = parts[0];
        String providedSignature = parts[1];

        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec key = new SecretKeySpec(
                jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256
        );
        mac.init(key);
        byte[] expectedSignatureBytes = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
        String expectedSignature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(expectedSignatureBytes);

        if (!constantTimeEquals(providedSignature, expectedSignature)) {
            throw new Exception("Invalid token signature");
        }

        byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
        Map<String, Object> payload = objectMapper.readValue(decoded, Map.class);

        Number exp = (Number) payload.get("exp");
        if (exp == null || Instant.now().isAfter(Instant.ofEpochSecond(exp.longValue()))) {
            throw new Exception("Token expired");
        }

        return payload;
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public Map<String, Object> createAccessPayload(Long userId, Iterable<String> roles) {
        long now = Instant.now().getEpochSecond();
        return Map.of(
                "userId", userId,
                "roles", roles,
                "iat", now,
                "exp", now + jwtConfig.getAccessExpiration()
        );
    }

    public Map<String, Object> createRefreshPayload(Long userId) {
        long now = Instant.now().getEpochSecond();
        return Map.of(
                "userId", userId,
                "iat", now,
                "exp", now + jwtConfig.getRefreshExpiration()
        );
    }

    // ✅ ИСПРАВЛЕНО: убран "мусор" из throws
    public Long extractUserId(String token) throws Exception {
        Map<String, Object> payload = validateToken(token);
        return ((Number) payload.get("userId")).longValue();
    }
}