package com.example.tpcleancode.controller;

import com.example.tpcleancode.entity.User;
import com.example.tpcleancode.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controleur d'administration.
 *
 * CORRECTIONS Securite :
 *
 * 1. GET /system exposait chemin, utilisateur OS, URL et credentials de la BDD
 *    AVANT: Map.of("os.user", System.getProperty("user.name"),
 *                   "db.url", env.getProperty("spring.datasource.url"),
 *                   "db.password", env.getProperty("spring.datasource.password"))
 *    APRES: endpoint supprime (information disclosure)
 *
 * 2. POST /cmd executait des commandes OS sans restriction
 *    AVANT: Runtime.getRuntime().exec(command);
 *    APRES: endpoint supprime (Remote Code Execution)
 *
 * 3. GET /users sans authentification - listait tous les utilisateurs avec leurs hashes
 *    AVANT: retournait la liste complete incluant passwordHash
 *    APRES: @PreAuthorize("hasRole('ADMIN')"), hash exclus de la reponse
 *
 * 4. deleteUser : controle de role present mais ignore
 *    AVANT: if (user.getRole() == ADMIN) { delete(user); } else { delete(user); }
 *    APRES: un ADMIN ne peut pas etre supprime par un autre ADMIN
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Liste les utilisateurs SANS exposer les donnees sensibles.
     * CORRECTION : @PreAuthorize("hasRole('ADMIN')") + hash exclu.
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<User> users = userRepository.findAll();

        // CORRECTION : ne pas retourner les hashes de mots de passe
        List<Map<String, Object>> sanitizedUsers = users.stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "active", user.isActive()
                ))
                .toList();

        return ResponseEntity.ok(sanitizedUsers);
    }

    /**
     * Supprime un utilisateur.
     *
     * CORRECTION Clean Code + Securite :
     * - Le controle de role est effectif (un ADMIN ne peut pas etre supprime)
     * AVANT : les deux branches du if faisaient la meme chose
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    // CORRECTION : on empeche la suppression d'un ADMIN
                    if (user.getRole() == com.example.tpcleancode.entity.Role.ADMIN) {
                        logger.warn("Tentative de suppression d'un compte ADMIN (id={})", id);
                        return ResponseEntity.badRequest()
                                .body("Impossible de supprimer un compte administrateur");
                    }
                    userRepository.delete(user);
                    logger.info("Utilisateur supprime : id={}", id);
                    return ResponseEntity.ok("Utilisateur supprime");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * ENDPOINTS SUPPRIMES pour raisons de securite :
     *
     * GET /system - Information Disclosure
     *   Exposait : System.getProperty("user.name"), "user.dir",
     *              datasource URL et credentials
     *   => SUPPRIME : ne jamais exposer d'informations systeme
     *
     * POST /cmd - Remote Code Execution (RCE)
     *   Executait : Runtime.getRuntime().exec(request.getBody())
     *   => SUPPRIME : ne jamais permettre l'execution de commandes OS
     */
}
