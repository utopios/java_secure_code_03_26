package com.example.tpcleancode.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controleur de rapports.
 *
 * CORRECTIONS Securite :
 *
 * 1. SSRF - GET /fetch?url= envoyait une requete HTTP vers n'importe quelle URL
 *    AVANT: URL url = new URL(request.getParameter("url"));
 *           InputStream is = url.openStream(); // SSRF !
 *    APRES: endpoint supprime - pas de proxy HTTP cote serveur
 *
 * 2. Path Traversal - GET /file?path= lisait n'importe quel fichier du serveur
 *    AVANT: Files.readAllBytes(Paths.get(request.getParameter("path")));
 *           Ex: /file?path=../../../../etc/passwd
 *    APRES: endpoint supprime - pas d'acces direct au systeme de fichiers
 *
 * 3. Open Redirect - GET /redirect?target= redirigeait vers n'importe quelle URL
 *    AVANT: return "redirect:" + request.getParameter("target");
 *           Ex: /redirect?target=https://evil.com
 *    APRES: endpoint supprime - pas de redirection ouverte
 *
 * Ce controleur ne conserve que les fonctionnalites metier legitimes
 * avec authentification et autorisation.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    /**
     * Genere un rapport de synthese (fonctionnalite metier legitime).
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        logger.info("Generation du rapport de synthese");

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Rapport genere avec succes",
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    /*
     * ENDPOINTS SUPPRIMES pour raisons de securite :
     *
     * GET /fetch?url= - SSRF (Server-Side Request Forgery)
     *   Permettait d'envoyer des requetes HTTP depuis le serveur vers
     *   n'importe quelle URL (interne ou externe).
     *   Risque : acces aux services internes (metadata cloud, BDD, etc.)
     *   => SUPPRIME
     *
     * GET /file?path= - Path Traversal / LFI (Local File Inclusion)
     *   Permettait de lire n'importe quel fichier du serveur.
     *   Ex: /file?path=../../../../etc/passwd
     *   Risque : lecture de fichiers sensibles (config, credentials, etc.)
     *   => SUPPRIME
     *
     * GET /redirect?target= - Open Redirect
     *   Permettait de rediriger l'utilisateur vers n'importe quelle URL.
     *   Ex: /redirect?target=https://phishing-site.com
     *   Risque : phishing, vol de credentials
     *   => SUPPRIME
     */
}
