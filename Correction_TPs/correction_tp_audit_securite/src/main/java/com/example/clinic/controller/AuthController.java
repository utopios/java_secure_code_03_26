package com.example.clinic.controller;

import com.example.clinic.entity.Doctor;
import com.example.clinic.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // CORRIGE #17 : Verifier si le compte est verrouille (brute force)
        if (authService.isAccountLocked(username)) {
            return ResponseEntity.status(429).body(Map.of(
                "error", "Compte temporairement verrouille"
            ));
        }

        Doctor doctor = authService.authenticate(username, password);

        if (doctor == null) {
            // CORRIGE #13 : Message GENERIQUE -- ne pas reveler si le user existe
            // AVANT : "Utilisateur " + username + " non trouve ou mot de passe incorrect"
            // -> permettait l'enumeration d'utilisateurs
            return ResponseEntity.status(401).body(Map.of(
                "error", "Identifiants invalides"
            ));
        }

        // CORRIGE #14 : Log SECURISE -- jamais de mot de passe dans les logs
        // AVANT : System.out.println("Login reussi pour " + username + " avec mot de passe " + password);
        log.info("Login reussi pour l'utilisateur id={}", doctor.getId());

        // CORRIGE #16 : Token aleatoire avec SecureRandom (32 octets d'entropie)
        // AVANT : "session-" + doctor.getId() -> predictible (session-1, session-2, session-3)
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // CORRIGE #15 : Pas de hash du mot de passe dans la reponse
        // AVANT : "password", doctor.getPassword() -> exposait le hash BCrypt
        return ResponseEntity.ok(Map.of(
            "username", doctor.getUsername(),
            "fullName", doctor.getFullName(),
            "role", doctor.getRole(),
            "token", token
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String fullName = body.get("fullName");
        String specialty = body.get("specialty");

        Doctor doctor = authService.register(username, password, fullName, specialty);
        log.info("Nouveau docteur enregistre id={}", doctor.getId());
        return ResponseEntity.ok(Map.of("id", doctor.getId(), "username", doctor.getUsername()));
    }
}
