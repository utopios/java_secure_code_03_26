package com.example.owaspdemo.demo;

import com.example.owaspdemo.entity.User;
import com.example.owaspdemo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A07:2021 - MANQUE DE CONTROLE D'ACCES AU NIVEAU FONCTIONNEL
 * ============================================================================
 *
 *  Se produit quand une application ne verifie pas les droits d'acces
 *  avant d'executer une action privilegiee.
 *
 *  Exemples :
 *  - Un utilisateur normal accede aux fonctions d'administration
 *  - L'interface cache le bouton "Admin" mais l'API est accessible
 *  - Pas de verification de role cote serveur
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET    /api/a07/vulnerable/admin/users
 *    VULNERABLE : DELETE /api/a07/vulnerable/admin/user/1
 *    SECURISE   : GET    /api/a07/secure/admin/users?currentUser=x
 *    SECURISE   : DELETE /api/a07/secure/admin/user/1?currentUser=x
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a07")
@Tag(name = "A07 - Controle d'acces")
public class A07_AccessControlDemo {

    private final UserRepository userRepository;

    public A07_AccessControlDemo(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ========================================================================
    //  ENDPOINTS D'ADMINISTRATION
    // ========================================================================

    /**
     * VULNERABLE : aucune verification de role.
     *
     * N'importe qui connaissant l'URL peut lister tous les utilisateurs.
     * Le bouton est peut-etre cache dans l'interface, mais l'API est ouverte.
     *
     * Attaque : GET /api/a07/vulnerable/admin/users
     * -> Retourne tous les utilisateurs, meme sans etre admin
     */
    @Operation(summary = "[VULNERABLE] Liste admin sans verification de role",
               description = "N'importe qui peut acceder a cette liste. Le bouton est cache dans l'UI mais l'API est ouverte.")
    @GetMapping("/vulnerable/admin/users")
    public Map<String, Object> vulnerableListUsers() {
        List<User> users = userRepository.findAll();
        return Map.of(
            "users", users,
            "warning", "Aucune verification de role ! N'importe qui peut acceder a cette liste."
        );
    }

    /**
     * VULNERABLE : suppression sans verification de role.
     *
     * Attaque : DELETE /api/a07/vulnerable/admin/user/2
     * -> Supprime Alice sans etre admin
     */
    @Operation(summary = "[VULNERABLE] Suppression sans verification de role",
               description = "Essayez id=2 pour supprimer Alice sans etre admin.")
    @DeleteMapping("/vulnerable/admin/user/{id}")
    public Map<String, Object> vulnerableDeleteUser(@Parameter(example = "2") @PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return Map.<String, Object>of(
                        "status", "Utilisateur supprime : " + user.getUsername(),
                        "warning", "Aucune verification de role pour cette action critique !"
                    );
                })
                .orElse(Map.of("error", "Utilisateur non trouve"));
    }

    /**
     * SECURISE : verification du role ADMIN avant chaque action.
     *
     * En production, le role viendrait du SecurityContext (JWT, session),
     * pas d'un parametre de requete.
     * Utiliser @PreAuthorize("hasRole('ADMIN')") de Spring Security.
     */
    @Operation(summary = "[SECURISE] Liste admin avec verification RBAC",
               description = "Essayez currentUser=bob -> refuse. currentUser=admin -> autorise.")
    @GetMapping("/secure/admin/users")
    public Map<String, Object> secureListUsers(@Parameter(example = "bob") @RequestParam String currentUser) {
        // Verifier que l'utilisateur est admin
        return userRepository.findByUsername(currentUser)
                .map(user -> {
                    if (!"ADMIN".equals(user.getRole())) {
                        return Map.<String, Object>of(
                            "error", "Acces refuse",
                            "message", "Cette fonctionnalite est reservee aux administrateurs",
                            "your_role", user.getRole()
                        );
                    }
                    return Map.<String, Object>of(
                        "users", userRepository.findAll(),
                        "info", "Acces autorise : role ADMIN verifie"
                    );
                })
                .orElse(Map.of("error", "Utilisateur non trouve"));
    }

    @Operation(summary = "[SECURISE] Suppression avec verification RBAC",
               description = "Seul un admin peut supprimer. Un admin ne peut pas se supprimer lui-meme.")
    @DeleteMapping("/secure/admin/user/{id}")
    public Map<String, Object> secureDeleteUser(@Parameter(example = "2") @PathVariable Long id,
                                                 @Parameter(example = "admin") @RequestParam String currentUser) {
        // Verifier que l'utilisateur est admin
        return userRepository.findByUsername(currentUser)
                .map(caller -> {
                    if (!"ADMIN".equals(caller.getRole())) {
                        return Map.<String, Object>of(
                            "error", "Acces refuse",
                            "message", "Seul un admin peut supprimer des utilisateurs",
                            "your_role", caller.getRole()
                        );
                    }

                    // Empecher un admin de se supprimer lui-meme
                    return userRepository.findById(id)
                            .map(target -> {
                                if (target.getUsername().equals(currentUser)) {
                                    return Map.<String, Object>of(
                                        "error", "Impossible de supprimer votre propre compte"
                                    );
                                }
                                userRepository.delete(target);
                                return Map.<String, Object>of(
                                    "status", "Utilisateur supprime : " + target.getUsername(),
                                    "info", "Action effectuee par admin : " + currentUser
                                );
                            })
                            .orElse(Map.of("error", "Utilisateur cible non trouve"));
                })
                .orElse(Map.of("error", "Utilisateur appelant non trouve"));
    }

    // ========================================================================
    //  EXEMPLE AVEC SPRING SECURITY (ANNOTATION)
    // ========================================================================

    /**
     * En production, utiliser les annotations Spring Security :
     *
     * @PreAuthorize("hasRole('ADMIN')")
     * @GetMapping("/admin/users")
     * public List<User> listUsers() {
     *     return userRepository.findAll();
     * }
     *
     * Spring verifie automatiquement le role dans le SecurityContext.
     * Pas besoin de verification manuelle.
     */
    @Operation(summary = "[INFO] Exemple @PreAuthorize Spring Security",
               description = "En production, utiliser @PreAuthorize(\"hasRole('ADMIN')\") pour la verification automatique.")
    @GetMapping("/example-annotation")
    public Map<String, Object> exampleAnnotation() {
        return Map.of(
            "code_production", "@PreAuthorize(\"hasRole('ADMIN')\")",
            "avantage", "Spring verifie automatiquement le role, impossible a contourner",
            "config_requise", "Activer @EnableMethodSecurity dans la config Spring Security"
        );
    }
}
