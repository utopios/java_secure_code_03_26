package com.example.owaspdemo.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A10:2021 - REDIRECTIONS ET TRANSFERTS NON VALIDES
 * ============================================================================
 *
 *  Se produit quand une application redirige l'utilisateur vers une URL
 *  fournie en parametre sans la valider.
 *
 *  L'attaquant peut rediriger la victime vers un site de phishing qui
 *  imite le site original pour voler des identifiants.
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET /api/a10/vulnerable/redirect?url=x
 *    SECURISE   : GET /api/a10/secure/redirect?url=x
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a10")
@Tag(name = "A10 - Redirections")
public class A10_UnvalidatedRedirectsDemo {

    // Domaines autorises pour les redirections
    private static final List<String> ALLOWED_DOMAINS = List.of(
        "company.com",
        "www.company.com",
        "app.company.com"
    );

    // ========================================================================
    //  REDIRECTION NON VALIDEE
    // ========================================================================

    /**
     * VULNERABLE : redirige vers n'importe quelle URL fournie par l'utilisateur.
     *
     * Attaque de phishing :
     *   Lien envoye a la victime :
     *   https://company.com/api/a10/vulnerable/redirect?url=https://evil-company.com/login
     *
     *   La victime voit "company.com" dans le lien et fait confiance.
     *   Elle est redirigee vers le faux site qui vole ses identifiants.
     */
    @GetMapping("/vulnerable/redirect")
    @Operation(summary = "[VULNERABLE] Open redirect",
               description = "Redirige vers n'importe quelle URL. Essayez url=https://evil.com/phishing. La victime voit company.com dans le lien et fait confiance.")
    public ResponseEntity<Void> vulnerableRedirect(@Parameter(example = "https://evil.com/phishing") @RequestParam String url) {
        // DANGEREUX : redirection vers n'importe quelle URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    /**
     * SECURISE : validation de l'URL avant redirection.
     *
     * Protections :
     * 1. Whitelist de domaines autorises
     * 2. Verification du protocole (HTTPS uniquement)
     * 3. Blocage des URL relatives qui pourraient etre manipulees
     */
    @GetMapping("/secure/redirect")
    @Operation(summary = "[SECURISE] Redirect avec whitelist",
               description = "Seuls les domaines company.com en HTTPS sont autorises. Essayez url=https://evil.com -> refuse.")
    public ResponseEntity<?> secureRedirect(@Parameter(example = "https://evil.com/phishing") @RequestParam String url) {
        try {
            URI uri = URI.create(url);

            // 1. Verifier que le protocole est HTTPS
            if (!"https".equals(uri.getScheme())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Seul le protocole HTTPS est autorise pour les redirections",
                    "provided", uri.getScheme()
                ));
            }

            // 2. Verifier que le domaine est dans la whitelist
            String host = uri.getHost();
            if (host == null || !ALLOWED_DOMAINS.contains(host)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Domaine non autorise pour la redirection",
                    "provided", host,
                    "allowed", ALLOWED_DOMAINS
                ));
            }

            // 3. Redirection autorisee
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(uri)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "URL invalide : " + e.getMessage()
            ));
        }
    }

    // ========================================================================
    //  ALTERNATIVE : REDIRECTION PAR CLE
    // ========================================================================

    /**
     * MEILLEURE APPROCHE : utiliser des cles au lieu d'URLs directes.
     *
     * L'utilisateur ne fournit qu'une cle, le serveur resout l'URL.
     * Impossible de rediriger vers un site non prevu.
     *
     * Exemple : GET /api/a10/secure/goto?target=dashboard
     */
    @GetMapping("/secure/goto")
    @Operation(summary = "[SECURISE] Redirect par cle",
               description = "L'utilisateur fournit une cle (dashboard, profile, help, logout), le serveur resout l'URL. Impossible de rediriger vers un site non prevu.")
    public ResponseEntity<?> secureGoto(@Parameter(example = "dashboard") @RequestParam String target) {
        // Map de cles -> URLs (hardcodee cote serveur)
        Map<String, String> redirectMap = Map.of(
            "dashboard", "https://app.company.com/dashboard",
            "profile", "https://app.company.com/profile",
            "help", "https://www.company.com/help",
            "logout", "https://company.com/logout"
        );

        String url = redirectMap.get(target);
        if (url == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Cible de redirection inconnue",
                "provided", target,
                "allowed", redirectMap.keySet()
            ));
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    /**
     * Informations sur les protections contre les redirections non validees.
     */
    @GetMapping("/info")
    @Operation(summary = "[INFO] Protections contre les redirections",
               description = "Description des attaques par open redirect et les protections : whitelist, cles, avertissement.")
    public Map<String, Object> info() {
        return Map.of(
            "description", "Les redirections non validees permettent le phishing",
            "attaque_type", Map.of(
                "phishing", "Rediriger vers un faux site pour voler des identifiants",
                "open_redirect", "Utiliser le domaine de confiance comme tremplin"
            ),
            "protections", Map.of(
                "whitelist", "Limiter les domaines de redirection autorises",
                "cles", "Utiliser des cles au lieu d'URLs (dashboard, profile, etc.)",
                "avertissement", "Afficher un avertissement avant de rediriger vers un site externe",
                "interdire", "Interdire les redirections vers des domaines externes"
            ),
            "spring_security", "Spring Security intercepte les redirections apres login "
                             + "et les valide automatiquement (SavedRequestAwareAuthenticationSuccessHandler)"
        );
    }
}
