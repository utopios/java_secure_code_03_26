package com.example.owaspdemo.demo;

import com.example.owaspdemo.entity.Product;
import com.example.owaspdemo.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A04:2021 - REFERENCES DIRECTES NON SECURISEES A DES OBJETS (IDOR)
 * ============================================================================
 *
 *  Se produit quand une application expose une reference interne (ID, nom de
 *  fichier) et ne verifie pas que l'utilisateur a le droit d'y acceder.
 *
 *  Exemple : un utilisateur change l'ID dans l'URL pour acceder aux donnees
 *  d'un autre utilisateur.
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET /api/a04/vulnerable/product/1
 *    SECURISE   : GET /api/a04/secure/product/1?currentUser=alice
 *    VULNERABLE : GET /api/a04/vulnerable/user/1
 *    SECURISE   : GET /api/a04/secure/user/1?currentUser=admin
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a04")
@Tag(name = "A04 - IDOR")
public class A04_InsecureDirectObjectRefDemo {

    private final ProductRepository productRepository;

    public A04_InsecureDirectObjectRefDemo(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ========================================================================
    //  IDOR SUR LES PRODUITS
    // ========================================================================

    /**
     * VULNERABLE : aucune verification de propriete.
     *
     * N'importe quel utilisateur peut acceder a n'importe quel produit
     * en changeant l'ID dans l'URL.
     *
     * Attaque : GET /api/a04/vulnerable/product/5
     * -> Retourne le "Document Confidentiel" de l'admin
     */
    @Operation(summary = "[VULNERABLE] Acces produit sans verification",
               description = "Essayez id=5 pour acceder au Document Confidentiel de l'admin. Aucune verification de propriete.")
    @GetMapping("/vulnerable/product/{id}")
    public Map<String, Object> vulnerableGetProduct(@Parameter(example = "5") @PathVariable Long id) {
        return productRepository.findById(id)
                .map(p -> Map.<String, Object>of(
                    "id", p.getId(),
                    "name", p.getName(),
                    "description", p.getDescription(),
                    "price", p.getPrice(),
                    "owner", p.getOwner(),
                    "warning", "Aucune verification de propriete ! N'importe qui peut voir ce produit."
                ))
                .orElse(Map.of("error", "Produit non trouve"));
    }

    /**
     * SECURISE : verification que l'utilisateur courant est le proprietaire.
     *
     * En production, le currentUser viendrait du SecurityContext (JWT, session),
     * pas d'un parametre de requete.
     */
    @Operation(summary = "[SECURISE] Acces produit avec verification proprietaire",
               description = "Essayez id=5 currentUser=bob -> refuse. id=5 currentUser=admin -> autorise.")
    @GetMapping("/secure/product/{id}")
    public Map<String, Object> secureGetProduct(@Parameter(example = "5") @PathVariable Long id,
                                                 @Parameter(example = "bob") @RequestParam String currentUser) {
        return productRepository.findById(id)
                .map(p -> {
                    // Verifier que l'utilisateur courant est le proprietaire
                    if (!p.getOwner().equals(currentUser) && !"admin".equals(currentUser)) {
                        return Map.<String, Object>of(
                            "error", "Acces refuse",
                            "message", "Vous n'etes pas le proprietaire de ce produit",
                            "info", "IDOR bloque : verification de propriete"
                        );
                    }
                    return Map.<String, Object>of(
                        "id", p.getId(),
                        "name", p.getName(),
                        "description", p.getDescription(),
                        "owner", p.getOwner(),
                        "info", "Acces autorise : vous etes le proprietaire ou admin"
                    );
                })
                .orElse(Map.of("error", "Produit non trouve"));
    }

    // ========================================================================
    //  IDOR SUR LES LISTES
    // ========================================================================

    /**
     * VULNERABLE : retourne TOUS les produits de la base, sans filtrage.
     */
    @Operation(summary = "[VULNERABLE] Liste TOUS les produits",
               description = "Retourne les produits de tous les utilisateurs, y compris les documents confidentiels.")
    @GetMapping("/vulnerable/products")
    public List<Product> vulnerableGetAllProducts() {
        return productRepository.findAll();
    }

    /**
     * SECURISE : retourne uniquement les produits de l'utilisateur courant.
     */
    @Operation(summary = "[SECURISE] Liste MES produits uniquement",
               description = "Filtrage par proprietaire. Essayez currentUser=alice pour voir seulement ses 2 produits.")
    @GetMapping("/secure/products")
    public Map<String, Object> secureGetMyProducts(@Parameter(example = "alice") @RequestParam String currentUser) {
        List<Product> myProducts = productRepository.findByOwner(currentUser);
        return Map.of(
            "owner", currentUser,
            "products", myProducts,
            "info", "Filtrage par proprietaire : chaque utilisateur ne voit que ses produits"
        );
    }
}
