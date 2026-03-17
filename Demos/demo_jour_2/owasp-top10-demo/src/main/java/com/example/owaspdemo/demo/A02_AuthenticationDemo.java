package com.example.owaspdemo.demo;

import com.example.owaspdemo.entity.User;
import com.example.owaspdemo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================================
 *  A02:2021 - VIOLATION DE L'AUTHENTIFICATION ET GESTION DES SESSIONS
 * ============================================================================
 *
 *  Se produit quand les mecanismes d'authentification sont mal implementes :
 *  - Mots de passe stockes en clair ou en MD5/SHA-256
 *  - Pas de protection contre le brute force
 *  - Tokens de session predictibles
 *  - Pas de MFA
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : POST /api/a02/vulnerable/login
 *    SECURISE   : POST /api/a02/secure/login
 *    VULNERABLE : GET  /api/a02/vulnerable/session
 *    SECURISE   : GET  /api/a02/secure/session
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a02")
@Tag(name = "A02 - Authentification")
public class A02_AuthenticationDemo {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Compteur de tentatives pour la protection brute force
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    // Compteur de session predictible (vulnerable)
    private int sessionCounter = 1000;

    public A02_AuthenticationDemo(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================================================================
    //  AUTHENTIFICATION
    // ========================================================================

    /**
     * VULNERABLE : mot de passe compare en clair, pas de protection brute force.
     *
     * Problemes :
     * 1. Le mot de passe est compare en clair (ici on simule en comparant directement)
     * 2. Aucune limite de tentatives -> brute force possible
     * 3. Le message d'erreur indique si c'est le username ou le password qui est faux
     *    -> permet l'enumeration d'utilisateurs
     */
    @Operation(
        summary = "[VULNERABLE] Login sans protection",
        description = "Pas de protection brute force. Le message d'erreur revele si c'est le username ou le password qui est faux (enumeration). Body: {\"username\":\"inexistant\",\"password\":\"x\"} -> 'Utilisateur inexistant'"
    )
    @PostMapping("/vulnerable/login")
    public Map<String, Object> vulnerableLogin(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        // VULNERABLE : message different selon que le user existe ou non
        // -> permet l'enumeration d'utilisateurs
        if (userOpt.isEmpty()) {
            return Map.of("status", "ECHEC", "message", "Utilisateur inexistant");
        }

        User user = userOpt.get();

        // VULNERABLE : pas de protection brute force
        // VULNERABLE : comparaison qui peut leaker via timing attack
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Map.of("status", "ECHEC", "message", "Mot de passe incorrect");
        }

        return Map.of(
            "status", "CONNECTE",
            "username", user.getUsername(),
            "warning", "Pas de protection brute force, enumeration possible"
        );
    }

    /**
     * SECURISE : BCrypt + protection brute force + messages generiques.
     *
     * Corrections :
     * 1. BCrypt pour le hachage (deja en place via PasswordEncoder)
     * 2. Limite de tentatives par username
     * 3. Message generique identique que le user existe ou non
     * 4. Nettoyage du compteur apres connexion reussie
     */
    @Operation(
        summary = "[SECURISE] Login avec BCrypt + brute force protection",
        description = "Message generique, compteur de tentatives (max 5). Body: {\"username\":\"admin\",\"password\":\"admin123\"}"
    )
    @PostMapping("/secure/login")
    public Map<String, Object> secureLogin(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Protection brute force : verifier le nombre de tentatives
        int attempts = loginAttempts.getOrDefault(username, 0);
        if (attempts >= MAX_ATTEMPTS) {
            return Map.of(
                "status", "BLOQUE",
                "message", "Compte temporairement verrouille apres " + MAX_ATTEMPTS + " tentatives"
            );
        }

        Optional<User> userOpt = userRepository.findByUsername(username);

        // Message GENERIQUE : ne pas reveler si le user existe
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            loginAttempts.merge(username, 1, Integer::sum);
            return Map.of("status", "ECHEC", "message", "Identifiants invalides");
        }

        // Connexion reussie : reset du compteur
        loginAttempts.remove(username);

        return Map.of(
            "status", "CONNECTE (securise)",
            "username", userOpt.get().getUsername(),
            "info", "BCrypt + brute force protection + message generique"
        );
    }

    // ========================================================================
    //  GENERATION DE TOKENS DE SESSION
    // ========================================================================

    /**
     * VULNERABLE : token de session predictible (compteur sequentiel).
     *
     * Un attaquant peut deviner les sessions suivantes : 1001, 1002, 1003...
     */
    @Operation(
        summary = "[VULNERABLE] Token de session predictible",
        description = "Token sequentiel : SESSION-1000, SESSION-1001... L'attaquant devine les sessions suivantes."
    )
    @GetMapping("/vulnerable/session")
    public Map<String, Object> vulnerableSession() {
        String token = "SESSION-" + (sessionCounter++);
        return Map.of(
            "token", token,
            "warning", "Token predictible ! L'attaquant peut deviner : SESSION-" + sessionCounter
        );
    }

    /**
     * SECURISE : token aleatoire cryptographiquement sur avec SecureRandom.
     *
     * 32 octets d'entropie = impossible a deviner.
     */
    @Operation(
        summary = "[SECURISE] Token aleatoire SecureRandom",
        description = "32 octets d'entropie. Impossible a predire."
    )
    @GetMapping("/secure/session")
    public Map<String, Object> secureSession() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        return Map.of(
            "token", token,
            "info", "SecureRandom (32 octets) : impossible a predire"
        );
    }
}
