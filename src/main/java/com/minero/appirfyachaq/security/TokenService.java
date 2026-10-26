package com.minero.appirfyachaq.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Token HMAC para sesiones temporales. La clave solo vive en variables de entorno. */
@Service
public class TokenService {
    private final byte[] secret;
    private final long expirationSeconds;

    public TokenService(@Value("${yachaq.auth.secret}") String secret,
                        @Value("${yachaq.auth.expiration-seconds:86400}") long expirationSeconds) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("YACHAQ_TOKEN_SECRET debe tener al menos 32 caracteres aleatorios");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    private String sign(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception e) {
            throw new IllegalStateException("Error de autenticacion", e);
        }
    }

    public String create(String idUser) {
        String payload = idUser + "|" + (Instant.now().getEpochSecond() + expirationSeconds);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(encoded);
    }

    public String validateAndGetUserId(String token) {
        try {
            if (token == null || token.length() > 2048) return null;
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2) return null;
            byte[] expected = sign(parts[0]).getBytes(StandardCharsets.US_ASCII);
            byte[] actual = parts[1].getBytes(StandardCharsets.US_ASCII);
            if (!MessageDigest.isEqual(expected, actual)) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] fields = payload.split("\\|", 2);
            if (fields.length != 2 || !fields[0].matches("[a-fA-F0-9-]{36}")) return null;
            if (Long.parseLong(fields[1]) <= Instant.now().getEpochSecond()) return null;
            return fields[0];
        } catch (Exception ignored) {
            return null;
        }
    }
}
