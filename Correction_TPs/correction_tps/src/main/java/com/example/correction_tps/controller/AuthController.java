package com.example.correction_tps.controller;

import com.example.correction_tps.service.AttackDetectionService;
import com.example.correction_tps.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AttackDetectionService attackDetectionService;
    private final SessionService sessionService;

    public AuthController(AttackDetectionService attackDetectionService,
                          SessionService sessionService) {
        this.attackDetectionService = attackDetectionService;
        this.sessionService = sessionService;
    }

    /**
     * Endpoint de login (public).
     * Enregistre l'IP de connexion pour la détection d'attaques.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, HttpServletRequest request) {
        // Enregistrer l'IP pour la détection de connexions multiples
        String ip = request.getRemoteAddr();
        attackDetectionService.recordLoginIp(username, ip);

        // Vérifier si le compte est suspect
        if (attackDetectionService.isSuspicious(username)) {
            return ResponseEntity.status(403)
                    .body("Compte temporairement bloqué pour activité suspecte");
        }

        // Configurer la session normale
        sessionService.configureSessionTimeout(request, "normal");

        return ResponseEntity.ok("Connexion réussie");
    }

    /**
     * Endpoint de register (public).
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username) {
        return ResponseEntity.ok("Inscription réussie pour: " + username);
    }

    /**
     * Endpoint de logout.
     * La session est invalidée et le cookie JSESSIONID supprimé (configuré dans SecurityConfig).
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        sessionService.invalidateSession(request);
        return ResponseEntity.ok("Déconnexion réussie");
    }
}
