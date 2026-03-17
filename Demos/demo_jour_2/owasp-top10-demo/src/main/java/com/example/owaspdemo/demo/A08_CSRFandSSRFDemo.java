package com.example.owaspdemo.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A08:2021 - CSRF (Cross-Site Request Forgery) & SSRF (Server-Side Request Forgery)
 * ============================================================================
 *
 *  CSRF : force un utilisateur authentifie a executer une action non voulue.
 *  Exemple : un lien malveillant declenche un virement bancaire.
 *
 *  SSRF : manipule le serveur pour qu'il fasse des requetes vers des
 *  ressources internes (metadata cloud, services internes).
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET  /api/a08/vulnerable/fetch?url=x
 *    SECURISE   : GET  /api/a08/secure/fetch?url=x
 *    INFO       : GET  /api/a08/csrf-explanation
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a08")
@Tag(name = "A08 - CSRF/SSRF")
public class A08_CSRFandSSRFDemo {

    // ========================================================================
    //  CSRF - EXPLICATION
    // ========================================================================

    /**
     * Explique le mecanisme d'une attaque CSRF et les protections.
     */
    @GetMapping("/csrf-explanation")
    @Operation(summary = "[INFO] Explication CSRF", description = "Explique le mecanisme d'attaque CSRF et les protections (token CSRF, SameSite cookie, Spring Security).")
    public Map<String, Object> csrfExplanation() {
        return Map.of(
            "description", "CSRF force un utilisateur authentifie a executer une action non voulue",
            "scenario", Map.of(
                "1_prerequis", "L'utilisateur est connecte a banque.com (cookie de session actif)",
                "2_attaque", "L'attaquant envoie un email avec un lien : <img src='https://banque.com/virement?to=hacker&amount=10000'>",
                "3_execution", "Le navigateur envoie la requete AVEC le cookie de session",
                "4_resultat", "Le virement est execute sans le consentement de l'utilisateur"
            ),
            "protections", Map.of(
                "csrf_token", "Ajouter un token CSRF unique a chaque formulaire (Spring Security le fait automatiquement)",
                "samesite_cookie", "Configurer SameSite=Strict sur les cookies de session",
                "verifier_referer", "Verifier le header Referer/Origin des requetes",
                "spring_security", "CsrfFilter est active par defaut dans Spring Security (ne PAS le desactiver !)"
            ),
            "code_spring", Map.of(
                "activer", "http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))",
                "desactiver_api_rest", "Pour les API REST avec JWT, CSRF n'est pas necessaire car pas de cookies de session",
                "warning", "Ne JAMAIS desactiver CSRF sur une application web classique avec cookies !"
            )
        );
    }

    // ========================================================================
    //  SSRF (Server-Side Request Forgery)
    // ========================================================================

    /**
     * VULNERABLE : le serveur fetche n'importe quelle URL fournie par l'utilisateur.
     *
     * Attaques possibles :
     *   GET /api/a08/vulnerable/fetch?url=http://169.254.169.254/latest/meta-data/
     *   -> Accede aux metadata AWS (cles d'acces, tokens)
     *
     *   GET /api/a08/vulnerable/fetch?url=http://localhost:8080/api/a07/vulnerable/admin/users
     *   -> Accede aux endpoints internes
     *
     *   GET /api/a08/vulnerable/fetch?url=file:///etc/passwd
     *   -> Lit des fichiers locaux
     */
    @GetMapping("/vulnerable/fetch")
    @Operation(summary = "[VULNERABLE] SSRF - fetch sans validation", description = "Le serveur fetche n'importe quelle URL. Essayez url=http://localhost:8080/api/a06/vulnerable/users pour acceder aux donnees internes.")
    public Map<String, Object> vulnerableFetch(@Parameter(example = "http://localhost:8080/api/a06/vulnerable/users") @RequestParam String url) {
        try {
            // DANGEREUX : aucune validation de l'URL
            URL targetUrl = URI.create(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            String body = new String(conn.getInputStream().readAllBytes());

            return Map.of(
                "url", url,
                "status", responseCode,
                "body", body.substring(0, Math.min(body.length(), 500)),
                "warning", "SSRF : le serveur peut acceder a des ressources internes !"
            );
        } catch (Exception e) {
            return Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * SECURISE : validation stricte de l'URL avant la requete.
     *
     * Protections :
     * 1. Whitelist de domaines autorises
     * 2. Bloquer les adresses internes (localhost, 10.x, 172.x, 192.168.x, 169.254.x)
     * 3. Bloquer les protocoles dangereux (file://, ftp://, gopher://)
     * 4. Resoudre le DNS et verifier que l'IP n'est pas interne
     */
    @GetMapping("/secure/fetch")
    @Operation(summary = "[SECURISE] SSRF - fetch avec whitelist", description = "Seuls les domaines autorises en HTTPS sont acceptes. localhost, IPs internes et protocole file:// sont bloques.")
    public Map<String, Object> secureFetch(@Parameter(example = "http://localhost:8080/api/a06/vulnerable/users") @RequestParam String url) {
        // 1. Whitelist de domaines autorises
        List<String> allowedDomains = List.of("api.github.com", "jsonplaceholder.typicode.com");

        try {
            URI uri = URI.create(url);

            // 2. Verifier le protocole (HTTPS uniquement)
            if (!"https".equals(uri.getScheme())) {
                return Map.of("error", "Seul le protocole HTTPS est autorise");
            }

            // 3. Verifier le domaine contre la whitelist
            String host = uri.getHost();
            if (host == null || allowedDomains.stream().noneMatch(host::equals)) {
                return Map.of(
                    "error", "Domaine non autorise : " + host,
                    "allowed", allowedDomains
                );
            }

            // 4. Resoudre le DNS et verifier que l'IP n'est pas interne
            InetAddress address = InetAddress.getByName(host);
            if (isInternalAddress(address)) {
                return Map.of("error", "L'adresse resolue est interne : " + address.getHostAddress());
            }

            // 5. Executer la requete
            URL targetUrl = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false); // Pas de redirect (anti-rebond SSRF)

            int responseCode = conn.getResponseCode();
            String body = new String(conn.getInputStream().readAllBytes());

            return Map.of(
                "url", url,
                "status", responseCode,
                "body", body.substring(0, Math.min(body.length(), 500)),
                "info", "SSRF protege : whitelist + blocage IP internes + HTTPS only"
            );
        } catch (Exception e) {
            return Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Verifie si une adresse IP est interne/privee.
     */
    private boolean isInternalAddress(InetAddress address) {
        return address.isLoopbackAddress()       // 127.0.0.1
            || address.isSiteLocalAddress()       // 10.x, 172.16-31.x, 192.168.x
            || address.isLinkLocalAddress()       // 169.254.x (metadata AWS/GCP/Azure)
            || address.isAnyLocalAddress();        // 0.0.0.0
    }
}
