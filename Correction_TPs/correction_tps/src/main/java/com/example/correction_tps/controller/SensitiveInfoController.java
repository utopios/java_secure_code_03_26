package com.example.correction_tps.controller;

import com.example.correction_tps.service.AttackDetectionService;
import com.example.correction_tps.service.RateLimitingService;
import com.example.correction_tps.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TP2 - Endpoints de modification d'informations sensibles.
 * Détecte les tentatives répétées de modification (mot de passe, email).
 */
@RestController
@RequestMapping("/api/account")
public class SensitiveInfoController {

    private final AttackDetectionService attackDetectionService;
    private final RateLimitingService rateLimitingService;
    private final SessionService sessionService;

    public SensitiveInfoController(AttackDetectionService attackDetectionService,
                                   RateLimitingService rateLimitingService,
                                   SessionService sessionService) {
        this.attackDetectionService = attackDetectionService;
        this.rateLimitingService = rateLimitingService;
        this.sessionService = sessionService;
    }

    /**
     * Modification du mot de passe.
     * Session hautement critique + détection de tentatives répétées.
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Enregistrer la tentative de modification sensible
        attackDetectionService.recordSensitiveModificationAttempt(username);

        if (attackDetectionService.hasExcessiveSensitiveModifications(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Trop de tentatives de modification. Compte temporairement bloqué.");
        }

        if (!rateLimitingService.allowCriticalOperation(username)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Limite atteinte. Réessayez plus tard.");
        }

        // Session hautement critique : 5 minutes
        sessionService.configureSessionTimeout(request, "highly_critical");

        // Renouveler la session après modification de données sensibles
        sessionService.renewSession(request);

        return ResponseEntity.ok("Mot de passe modifié avec succès");
    }

    /**
     * Modification de l'email.
     * Session critique + détection de tentatives répétées.
     */
    @PostMapping("/change-email")
    public ResponseEntity<String> changeEmail(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        attackDetectionService.recordSensitiveModificationAttempt(username);

        if (attackDetectionService.hasExcessiveSensitiveModifications(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Trop de tentatives de modification. Compte temporairement bloqué.");
        }

        if (!rateLimitingService.allowCriticalOperation(username)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Limite atteinte. Réessayez plus tard.");
        }

        // Session critique : 20 minutes
        sessionService.configureSessionTimeout(request, "critical");

        return ResponseEntity.ok("Email modifié avec succès");
    }
}
