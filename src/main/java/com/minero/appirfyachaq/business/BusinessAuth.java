package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestForgotPassword;
import com.minero.appirfyachaq.dto.request.RequestLogin;
import com.minero.appirfyachaq.dto.request.RequestRegister;
import com.minero.appirfyachaq.dto.request.RequestResetPassword;
import com.minero.appirfyachaq.dto.response.ResponseForgotPassword;
import com.minero.appirfyachaq.dto.response.ResponseLogin;
import com.minero.appirfyachaq.dto.response.ResponseRegister;
import com.minero.appirfyachaq.dto.response.ResponseResetPassword;
import com.minero.appirfyachaq.entity.EntityUser;
import com.minero.appirfyachaq.repository.RepositoryUser;
import com.minero.appirfyachaq.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BusinessAuth {

    private static final Logger log = LoggerFactory.getLogger(BusinessAuth.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final int CODE_MINUTES = 15;
    private static final int COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from:onboarding@resend.dev}")
    private String resendFrom;

    private final RepositoryUser userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public BusinessAuth(RepositoryUser userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.tokenService = tokenService;
    }

    public ResponseLogin login(RequestLogin request) {
        ResponseLogin response = new ResponseLogin();

        EntityUser user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.error("Credenciales incorrectas.");
            return response;
        }

        if (user.getStatus() != null && !user.getStatus()) {
            response.error("Su cuenta está desactivada.");
            return response;
        }

        String token = tokenService.create(user.getIdUser());

        response.success("Login exitoso");
        response.setIdUser(user.getIdUser());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setIsPremium(false);
        response.setToken(token);

        return response;
    }

    public ResponseRegister register(RequestRegister request) {
        ResponseRegister response = new ResponseRegister();

        if (userRepository.existsByEmail(request.getEmail())) {
            response.error("El correo ya está registrado.");
            return response;
        }

        EntityUser newUser = EntityUser.builder()
                .idUser(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("minero")
                .createdAt(LocalDateTime.now())
                .status(true)
                .build();

        userRepository.save(newUser);

        String token = tokenService.create(newUser.getIdUser());

        response.success("Usuario registrado exitosamente.");
        response.setIdUser(newUser.getIdUser());
        response.setName(newUser.getName());
        response.setEmail(newUser.getEmail());
        response.setRole(newUser.getRole());
        response.setIsPremium(false);
        response.setToken(token);

        return response;
    }

    public ResponseForgotPassword forgotPassword(RequestForgotPassword request) {
        ResponseForgotPassword response = new ResponseForgotPassword();

        EntityUser user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            response.error("El correo no está registrado.");
            return response;
        }
        if (user.getStatus() != null && !user.getStatus()) {
            response.error("Su cuenta está desactivada.");
            return response;
        }

        // Cooldown: si se generó un código hace menos de 60s, no genera otro
        LocalDateTime exp = user.getResetCodeExpiresAt();
        if (exp != null && exp.minusMinutes(CODE_MINUTES)
                .plusSeconds(COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
            response.error("Espera " + COOLDOWN_SECONDS + " segundos antes de pedir otro código.");
            return response;
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        user.setResetCodeHash(passwordEncoder.encode(code));
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(CODE_MINUTES));
        user.setResetAttempts(0);
        userRepository.save(user);

        String error = sendResetCode(user.getEmail(), code);
        if (error != null) {
            // Si el correo no salió, borra el código para no bloquear al usuario con el cooldown
            user.setResetCodeHash(null);
            user.setResetCodeExpiresAt(null);
            userRepository.save(user);
            response.error(error);
            return response;
        }

        response.success("Código enviado a tu correo.");
        return response;
    }

    public ResponseResetPassword resetPassword(RequestResetPassword request) {
        ResponseResetPassword response = new ResponseResetPassword();

        EntityUser user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            response.error("El correo no está registrado.");
            return response;
        }
        if (user.getResetCodeHash() == null
                || user.getResetCodeExpiresAt() == null
                || user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())) {
            response.error("El código no existe o expiró. Solicita uno nuevo.");
            return response;
        }

        int attempts = user.getResetAttempts() == null ? 0 : user.getResetAttempts();
        if (attempts >= MAX_ATTEMPTS) {
            response.error("Demasiados intentos. Solicita un código nuevo.");
            return response;
        }

        if (!passwordEncoder.matches(request.getCode(), user.getResetCodeHash())) {
            user.setResetAttempts(attempts + 1);
            userRepository.save(user);
            response.error("Código incorrecto. Intentos restantes: " + (MAX_ATTEMPTS - attempts - 1));
            return response;
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCodeHash(null);
        user.setResetCodeExpiresAt(null);
        user.setResetAttempts(0);
        userRepository.save(user);

        response.success("Contraseña actualizada.");
        return response;
    }

    /** Envía el correo. Devuelve null si salió bien, o el motivo del error. */
    private String sendResetCode(String email, String code) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.error("RESEND_API_KEY no configurada.");
            return "El servicio de correo no está configurado.";
        }
        try {
            String body = "{\"from\":\"" + esc(resendFrom) + "\","
                    + "\"to\":[\"" + esc(email) + "\"],"
                    + "\"subject\":\"Código de recuperación\","
                    + "\"html\":\"<p>Tu código es <strong>" + code + "</strong>. Vence en "
                    + CODE_MINUTES + " minutos.</p>\"}";

            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 300) {
                log.error("Resend respondió {}: {}", res.statusCode(), res.body());
                return "No se pudo enviar el correo. Resend respondió " + res.statusCode() + ": " + res.body();
            }
            log.info("Correo de recuperación enviado a {}", email);
            return null;
        } catch (Exception e) {
            log.error("Error enviando correo de recuperación", e);
            return "No se pudo enviar el correo: " + e.getMessage();
        }
    }

    // Escapa comillas y barras para que el texto no rompa el JSON
    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}