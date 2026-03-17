package com.example.owaspdemo.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================================
 *  A05:2021 - MAUVAISE CONFIGURATION DE SECURITE
 * ============================================================================
 *
 *  Se produit quand la securite n'est pas correctement configuree :
 *  - Fonctionnalites inutiles activees (console H2, stack traces)
 *  - Comptes par defaut non modifies
 *  - Headers de securite manquants
 *  - Messages d'erreur trop detailles
 *  - CORS trop permissif
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET /api/a05/vulnerable/error
 *    SECURISE   : GET /api/a05/secure/error
 *    INFO       : GET /api/a05/headers-check
 *    INFO       : GET /api/a05/config-check
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a05")
@Tag(name = "A05 - Misconfiguration")
public class A05_SecurityMisconfigDemo {

    // ========================================================================
    //  MESSAGES D'ERREUR
    // ========================================================================

    /**
     * VULNERABLE : stack trace complete exposee a l'utilisateur.
     *
     * Un attaquant apprend :
     * - Le framework utilise (Spring Boot)
     * - La version de Java
     * - La structure des packages
     * - Les noms de classes internes
     * - Les requetes SQL executees
     */
    @Operation(summary = "[VULNERABLE] Stack trace exposee",
               description = "Retourne la stack trace Java complete. L'attaquant apprend le framework, la version Java, les noms de classes internes.")
    @GetMapping("/vulnerable/error")
    public Map<String, Object> vulnerableError() {
        try {
            // Simuler une erreur
            int result = 1 / 0;
            return Map.of("result", result);
        } catch (Exception e) {
            // DANGEREUX : retourner la stack trace complete
            return Map.of(
                "error", e.getClass().getName(),
                "message", e.getMessage(),
                "stackTrace", java.util.Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList(),
                "warning", "La stack trace revele des details internes de l'application !"
            );
        }
    }

    /**
     * SECURISE : message d'erreur generique sans details techniques.
     *
     * Les details sont logues cote serveur (pas envoyes au client).
     */
    @Operation(summary = "[SECURISE] Message d'erreur generique",
               description = "Retourne un message generique + une reference. Les details sont logues cote serveur.")
    @GetMapping("/secure/error")
    public Map<String, Object> secureError() {
        try {
            int result = 1 / 0;
            return Map.of("result", result);
        } catch (Exception e) {
            // SECURISE : log cote serveur, message generique cote client
            // En production : logger.error("Erreur interne", e);
            System.err.println("[LOG SERVEUR] Erreur interne : " + e.getMessage());

            return Map.of(
                "error", "Une erreur interne est survenue",
                "reference", "ERR-" + System.currentTimeMillis(),
                "info", "Les details sont logues cote serveur, pas envoyes au client"
            );
        }
    }

    // ========================================================================
    //  VERIFICATION DES HEADERS DE SECURITE
    // ========================================================================

    /**
     * Affiche les headers de securite recommandes et leur statut.
     */
    @Operation(summary = "[INFO] Headers de securite recommandes",
               description = "Liste les headers HTTP de securite a configurer : CSP, HSTS, X-Frame-Options, etc.")
    @GetMapping("/headers-check")
    public Map<String, Object> headersCheck() {
        Map<String, Object> headers = new LinkedHashMap<>();

        headers.put("Content-Security-Policy", Map.of(
            "recommande", "default-src 'self'; script-src 'self'; object-src 'none'",
            "role", "Empeche le chargement de scripts/ressources externes (anti-XSS)"
        ));
        headers.put("X-Content-Type-Options", Map.of(
            "recommande", "nosniff",
            "role", "Empeche le navigateur de deviner le type MIME"
        ));
        headers.put("X-Frame-Options", Map.of(
            "recommande", "DENY",
            "role", "Empeche l'inclusion dans une iframe (anti-clickjacking)"
        ));
        headers.put("Strict-Transport-Security", Map.of(
            "recommande", "max-age=31536000; includeSubDomains",
            "role", "Force HTTPS pendant 1 an"
        ));
        headers.put("Referrer-Policy", Map.of(
            "recommande", "strict-origin-when-cross-origin",
            "role", "Limite les informations envoyees dans le header Referer"
        ));
        headers.put("Permissions-Policy", Map.of(
            "recommande", "camera=(), microphone=(), geolocation=()",
            "role", "Desactive l'acces aux peripheriques sensibles"
        ));

        return Map.of("headers_de_securite", headers);
    }

    // ========================================================================
    //  VERIFICATION DE CONFIGURATION
    // ========================================================================

    /**
     * Liste les erreurs de configuration courantes a verifier.
     */
    @Operation(summary = "[INFO] Audit de configuration",
               description = "Liste les erreurs de configuration detectees dans cette application : console H2, CSRF desactive, stack traces, etc.")
    @GetMapping("/config-check")
    public Map<String, Object> configCheck() {
        Map<String, Object> checks = new LinkedHashMap<>();

        checks.put("console_h2", Map.of(
            "status", "ACTIVEE (spring.h2.console.enabled=true)",
            "risque", "CRITIQUE : permet l'execution de SQL arbitraire",
            "correction", "Desactiver en production ou restreindre l'acces"
        ));
        checks.put("stack_traces", Map.of(
            "status", "Visibles dans les reponses d'erreur",
            "risque", "MOYEN : revele la structure interne de l'application",
            "correction", "server.error.include-stacktrace=never"
        ));
        checks.put("csrf", Map.of(
            "status", "DESACTIVE (csrf.disable())",
            "risque", "ELEVE : permet les attaques CSRF",
            "correction", "Activer CSRF avec CookieCsrfTokenRepository"
        ));
        checks.put("cors", Map.of(
            "status", "A verifier",
            "risque", "ELEVE si * est autorise",
            "correction", "Limiter les origines autorisees explicitement"
        ));
        checks.put("comptes_defaut", Map.of(
            "status", "admin/admin123 dans DataInitializer",
            "risque", "CRITIQUE : identifiants par defaut",
            "correction", "Forcer le changement au premier login"
        ));

        return Map.of(
            "misconfigurations_detectees", checks,
            "note", "En production, utiliser un scanner comme OWASP ZAP pour detecter automatiquement"
        );
    }
}
