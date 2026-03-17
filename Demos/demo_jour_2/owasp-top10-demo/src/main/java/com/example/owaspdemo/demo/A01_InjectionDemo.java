package com.example.owaspdemo.demo;

import com.example.owaspdemo.entity.User;
import com.example.owaspdemo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A01:2021 - INJECTION (SQL, LDAP, OS Command)
 * ============================================================================
 *
 *  L'injection se produit quand des donnees non fiables sont envoyees a un
 *  interpreteur (SQL, LDAP, OS) dans le cadre d'une commande ou d'une requete.
 *
 *  L'attaquant peut lire/modifier/supprimer des donnees, voire executer des
 *  commandes systeme.
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET /api/a01/vulnerable/login?username=x&password=y
 *    SECURISE   : GET /api/a01/secure/login?username=x&password=y
 *    VULNERABLE : GET /api/a01/vulnerable/search?name=x
 *    SECURISE   : GET /api/a01/secure/search?name=x
 *    VULNERABLE : GET /api/a01/vulnerable/exec?cmd=x
 *    SECURISE   : GET /api/a01/secure/exec?cmd=x
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a01")
@Tag(name = "A01 - Injection")
public class A01_InjectionDemo {

    private final EntityManager entityManager;
    private final UserRepository userRepository;

    public A01_InjectionDemo(EntityManager entityManager, UserRepository userRepository) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
    }

    // ========================================================================
    //  SQL INJECTION
    // ========================================================================

    /**
     * VULNERABLE : concatenation de la saisie utilisateur dans la requete SQL.
     *
     * Attaque possible :
     *   GET /api/a01/vulnerable/login?username=admin'--&password=nimportequoi
     *   GET /api/a01/vulnerable/login?username=' OR '1'='1&password=' OR '1'='1
     *
     * Le "--" commente le reste de la requete, donc le mot de passe est ignore.
     * "OR 1=1" rend la condition toujours vraie -> retourne tous les utilisateurs.
     */
    @Operation(
        summary = "[VULNERABLE] Login par SQL Injection",
        description = """
            Concatenation directe dans la requete JPQL. Testez avec :
            - username: `' OR '1'='1`  password: `' OR '1'='1`  --> Bypass total, retourne l'admin
            - username: `admin`  password: `admin` --> Login normal (echec car mot de passe hache)
            """
    )
    @GetMapping("/vulnerable/login")
    public Map<String, Object> vulnerableLogin(
            @Parameter(description = "Essayez : `' OR '1'='1`", example = "' OR '1'='1") @RequestParam String username,
            @Parameter(description = "Essayez : `' OR '1'='1`", example = "' OR '1'='1") @RequestParam String password) {
        // DANGEREUX : concatenation directe dans la requete SQL
        String jpql = "SELECT u FROM User u WHERE u.username = '" + username
                     + "' AND u.password = '" + password + "'";

        Query query = entityManager.createQuery(jpql);

        @SuppressWarnings("unchecked")
        List<User> users = query.getResultList();

        if (!users.isEmpty()) {
            User user = users.get(0);
            return Map.of(
                "status", "CONNECTE (vulnerable)",
                "username", user.getUsername(),
                "role", user.getRole(),
                "warning", "Cette requete est vulnerable a l'injection SQL !"
            );
        }
        return Map.of("status", "ECHEC", "message", "Identifiants invalides");
    }

    /**
     * SECURISE : utilisation de requetes parametrees (prepared statements).
     *
     * Les parametres sont automatiquement echappes par JPA.
     * L'attaque admin'-- ne fonctionne plus car les guillemets sont echappes.
     */
    @Operation(
        summary = "[SECURISE] Login avec requete parametree",
        description = "La meme injection `' OR '1'='1` ne fonctionne plus. Les parametres sont echappes par JPA."
    )
    @GetMapping("/secure/login")
    public Map<String, Object> secureLogin(
            @Parameter(example = "' OR '1'='1") @RequestParam String username,
            @Parameter(example = "' OR '1'='1") @RequestParam String password) {
        // SECURISE : requete parametree via Spring Data JPA
        return userRepository.findByCredentials(username, password)
                .map(user -> Map.<String, Object>of(
                    "status", "CONNECTE (securise)",
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "info", "Requete parametree : l'injection est impossible"
                ))
                .orElse(Map.of("status", "ECHEC", "message", "Identifiants invalides"));
    }

    // ========================================================================
    //  INJECTION DE RECHERCHE
    // ========================================================================

    /**
     * VULNERABLE : injection dans une requete de recherche.
     *
     * Attaque : GET /api/a01/vulnerable/search?name=' OR '1'='1
     * Retourne TOUS les utilisateurs de la base.
     */
    @Operation(summary = "[VULNERABLE] Recherche par injection", description = "Essayez name = `' OR '1'='1` pour lister tous les users.")
    @GetMapping("/vulnerable/search")
    public List<?> vulnerableSearch(@Parameter(example = "' OR '1'='1") @RequestParam String name) {
        String jpql = "SELECT u FROM User u WHERE u.username LIKE '%" + name + "%'";
        return entityManager.createQuery(jpql).getResultList();
    }

    /**
     * SECURISE : parametre echappe automatiquement.
     */
    @Operation(summary = "[SECURISE] Recherche parametree", description = "L'injection est impossible, Spring Data genere un prepared statement.")
    @GetMapping("/secure/search")
    public List<User> secureSearch(@Parameter(example = "company") @RequestParam String name) {
        // Spring Data JPA genere une requete parametree
        return userRepository.findByEmailContainingIgnoreCase(name);
    }

    // ========================================================================
    //  INJECTION DE COMMANDE OS
    // ========================================================================

    /**
     * VULNERABLE : execution de commande systeme avec saisie utilisateur.
     *
     * Attaque : GET /api/a01/vulnerable/exec?cmd=ls;cat /etc/passwd
     * Le ";" permet de chainer une commande arbitraire.
     */
    @Operation(summary = "[VULNERABLE] Execution de commande OS", description = "Essayez cmd = `ls;cat /etc/passwd` pour chainer une commande malveillante.")
    @GetMapping("/vulnerable/exec")
    public Map<String, Object> vulnerableExec(@Parameter(example = "ls;whoami") @RequestParam String cmd) {
        try {
            // DANGEREUX : execution directe de la commande
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            String output = new String(process.getInputStream().readAllBytes());
            return Map.of("output", output, "warning", "Injection de commande possible !");
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * SECURISE : whitelist de commandes autorisees.
     *
     * Seules les commandes prevues sont executables.
     * Aucune saisie utilisateur n'est passee directement au shell.
     */
    @Operation(summary = "[SECURISE] Commande OS par whitelist", description = "Seules `date`, `uptime`, `hostname` sont autorisees. Essayez `cat /etc/passwd` -> refuse.")
    @GetMapping("/secure/exec")
    public Map<String, Object> secureExec(@Parameter(example = "date") @RequestParam String cmd) {
        // Whitelist stricte : seules ces commandes sont autorisees
        Map<String, String[]> allowedCommands = Map.of(
            "date", new String[]{"date"},
            "uptime", new String[]{"uptime"},
            "hostname", new String[]{"hostname"}
        );

        if (!allowedCommands.containsKey(cmd)) {
            return Map.of("error", "Commande non autorisee. Commandes valides : " + allowedCommands.keySet());
        }

        try {
            Process process = Runtime.getRuntime().exec(allowedCommands.get(cmd));
            String output = new String(process.getInputStream().readAllBytes());
            return Map.of("output", output, "info", "Whitelist : seules les commandes prevues sont autorisees");
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
