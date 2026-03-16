package com.example.correction_tps.controller;

import com.example.correction_tps.service.AttackDetectionService;
import com.example.correction_tps.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AttackDetectionService attackDetectionService;
    private final SessionService sessionService;

    public AdminController(AttackDetectionService attackDetectionService,
                           SessionService sessionService) {
        this.attackDetectionService = attackDetectionService;
        this.sessionService = sessionService;
    }

    /**
     * Consulter les logs (ADMIN et SUPER_ADMIN).
     */
    @GetMapping("/logs")
    public ResponseEntity<String> viewLogs() {
        return ResponseEntity.ok("Accès aux logs autorisé");
    }

    /**
     * Gérer les utilisateurs (ADMIN et SUPER_ADMIN).
     */
    @GetMapping("/users")
    public ResponseEntity<String> manageUsers() {
        return ResponseEntity.ok("Liste des utilisateurs");
    }

    /**
     * Vérifier si un utilisateur a un comportement suspect (ADMIN).
     */
    @GetMapping("/suspicious/{username}")
    public ResponseEntity<String> checkSuspicious(@PathVariable String username) {
        boolean suspicious = attackDetectionService.isSuspicious(username);
        return ResponseEntity.ok("Utilisateur suspect: " + suspicious);
    }

    /**
     * Réinitialiser les compteurs de détection d'attaque pour un utilisateur.
     */
    @PostMapping("/reset-alerts/{username}")
    public ResponseEntity<String> resetAlerts(@PathVariable String username, HttpServletRequest request) {
        // Session hautement critique pour les opérations admin sensibles
        sessionService.configureSessionTimeout(request, "highly_critical");
        attackDetectionService.resetCounters(username);
        return ResponseEntity.ok("Compteurs réinitialisés pour: " + username);
    }
}
