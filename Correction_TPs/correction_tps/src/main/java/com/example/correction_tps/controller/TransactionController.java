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

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final RateLimitingService rateLimitingService;
    private final SessionService sessionService;
    private final AttackDetectionService attackDetectionService;

    public TransactionController(RateLimitingService rateLimitingService,
                                 SessionService sessionService,
                                 AttackDetectionService attackDetectionService) {
        this.rateLimitingService = rateLimitingService;
        this.sessionService = sessionService;
        this.attackDetectionService = attackDetectionService;
    }

    /**
     * Opération normale : limitée à 10 transactions toutes les 10 minutes.
     */
    @PostMapping("/normal")
    public ResponseEntity<String> performNormalTransaction(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (attackDetectionService.isSuspicious(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Compte bloqué pour comportement suspect");
        }

        if (!rateLimitingService.allowNormalOperation(username)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Limite de transactions normales atteinte. Réessayez plus tard.");
        }

        sessionService.configureSessionTimeout(request, "normal");

        return ResponseEntity.ok("Transaction normale effectuée");
    }

    /**
     * Opération critique : limitée à 5 transactions par heure.
     * Accessible uniquement aux rôles ADMIN et SUPER_ADMIN (configuré dans SecurityConfig).
     */
    @PostMapping("/critical/payment")
    public ResponseEntity<String> performCriticalTransaction(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (attackDetectionService.isSuspicious(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Compte bloqué pour comportement suspect");
        }

        if (!rateLimitingService.allowCriticalOperation(username)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Limite de transactions critiques atteinte. Réessayez plus tard.");
        }

        // Session critique : timeout réduit à 20 minutes
        sessionService.configureSessionTimeout(request, "critical");

        return ResponseEntity.ok("Transaction critique effectuée");
    }
}
