package com.example.owaspdemo.demo;

import com.example.owaspdemo.entity.User;
import com.example.owaspdemo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 *  A06:2021 - EXPOSITION DE DONNEES SENSIBLES
 * ============================================================================
 *
 *  Se produit quand une application ne protege pas correctement les donnees
 *  sensibles : mots de passe, numeros de carte, donnees personnelles.
 *
 *  Problemes courants :
 *  - Donnees sensibles dans les reponses API (mot de passe, email)
 *  - Chiffrement faible ou absent
 *  - Mots de passe haches en MD5/SHA-256 (sans sel)
 *  - Logs contenant des donnees sensibles
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET /api/a06/vulnerable/users
 *    SECURISE   : GET /api/a06/secure/users
 *    VULNERABLE : POST /api/a06/vulnerable/hash-password
 *    SECURISE   : POST /api/a06/secure/encrypt
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a06")
@Tag(name = "A06 - Donnees sensibles")
public class A06_SensitiveDataExposureDemo {

    private final UserRepository userRepository;

    public A06_SensitiveDataExposureDemo(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ========================================================================
    //  EXPOSITION DE DONNEES DANS LES API
    // ========================================================================

    /**
     * VULNERABLE : retourne TOUTES les informations des utilisateurs,
     * y compris les mots de passe haches et les emails.
     *
     * Problemes :
     * 1. Le hash du mot de passe est expose (peut etre attaque offline)
     * 2. Les emails sont exposes (RGPD, spam, phishing)
     * 3. Les roles sont exposes (permet de cibler les admins)
     */
    @Operation(summary = "[VULNERABLE] Liste users avec mots de passe",
               description = "Retourne TOUTES les infos : hash BCrypt, emails, roles. Le hash peut etre attaque offline.")
    @GetMapping("/vulnerable/users")
    public List<Map<String, Object>> vulnerableGetUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("password", user.getPassword());  // DANGEREUX : hash expose
                    map.put("email", user.getEmail());         // DANGEREUX : PII expose
                    map.put("role", user.getRole());           // DANGEREUX : role expose
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * SECURISE : retourne uniquement les informations non sensibles.
     *
     * Principes :
     * 1. Ne jamais exposer le mot de passe (meme hache)
     * 2. Masquer les emails (a****@company.com)
     * 3. Ne pas exposer les roles sauf si necessaire
     */
    @Operation(summary = "[SECURISE] Liste users sans donnees sensibles",
               description = "Mot de passe exclu, email masque (a****@company.com), role exclu. Principe du moindre privilege.")
    @GetMapping("/secure/users")
    public Map<String, Object> secureGetUsers() {
        List<Map<String, Object>> safeUsers = userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    // Pas de mot de passe
                    map.put("email", maskEmail(user.getEmail())); // Email masque
                    // Pas de role
                    return map;
                })
                .collect(Collectors.toList());

        return Map.of(
            "users", safeUsers,
            "info", "Mot de passe, role exclus. Email masque. Principe du moindre privilege."
        );
    }

    // ========================================================================
    //  HACHAGE DE MOTS DE PASSE
    // ========================================================================

    /**
     * VULNERABLE : hachage MD5 sans sel.
     *
     * Problemes :
     * 1. MD5 est rapide (~10 milliards de hash/sec sur GPU) -> brute force facile
     * 2. Pas de sel -> rainbow tables applicables
     * 3. Deux utilisateurs avec le meme mot de passe ont le meme hash
     */
    @Operation(summary = "[VULNERABLE] Hachage MD5/SHA-256 sans sel",
               description = "MD5 = 10 milliards hash/sec sur GPU. SHA-256 sans sel = rainbow tables. Body: {\"password\":\"admin123\"}")
    @PostMapping("/vulnerable/hash-password")
    public Map<String, Object> vulnerableHash(@RequestBody Map<String, String> body) throws Exception {
        String password = body.get("password");

        // DANGEREUX : MD5 sans sel
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(password.getBytes());
        String md5Hash = Base64.getEncoder().encodeToString(hash);

        // DANGEREUX : SHA-256 sans sel (a peine mieux)
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] sha256Hash = sha.digest(password.getBytes());
        String sha256 = Base64.getEncoder().encodeToString(sha256Hash);

        return Map.of(
            "md5", md5Hash,
            "sha256", sha256,
            "warning", "MD5 et SHA-256 sans sel sont INTERDITS pour les mots de passe. "
                     + "Utiliser BCrypt ou Argon2id."
        );
    }

    // ========================================================================
    //  CHIFFREMENT DE DONNEES
    // ========================================================================

    /**
     * SECURISE : chiffrement AES-GCM pour les donnees sensibles.
     *
     * Demontre le bon usage du chiffrement pour proteger des donnees
     * en transit ou au repos.
     */
    @Operation(summary = "[SECURISE] Chiffrement AES-256-GCM",
               description = "Chiffrement authentifie avec IV aleatoire. Body: {\"data\":\"numero carte 4532-xxxx-xxxx-1234\"}")
    @PostMapping("/secure/encrypt")
    public Map<String, Object> secureEncrypt(@RequestBody Map<String, String> body) throws Exception {
        String data = body.get("data");

        // Generer une cle AES-256
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();

        // Generer un IV de 12 octets
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        // Chiffrer avec AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(data.getBytes());

        return Map.of(
            "original_length", data.length(),
            "encrypted_length", encrypted.length,
            "iv", Base64.getEncoder().encodeToString(iv),
            "encrypted", Base64.getEncoder().encodeToString(encrypted),
            "algorithm", "AES-256-GCM (chiffrement authentifie)",
            "info", "Les donnees sensibles doivent etre chiffrees avant stockage/transport"
        );
    }

    // ========================================================================
    //  LOGGING DANGEREUX
    // ========================================================================

    /**
     * VULNERABLE : log de donnees sensibles.
     */
    @Operation(summary = "[VULNERABLE] Log avec donnees de carte",
               description = "Le numero de carte et le CVV sont logues en clair. Body: {\"cardNumber\":\"4532111122223333\",\"cvv\":\"123\"}")
    @PostMapping("/vulnerable/log-payment")
    public Map<String, Object> vulnerableLogPayment(@RequestBody Map<String, String> body) {
        String cardNumber = body.get("cardNumber");
        String cvv = body.get("cvv");

        // DANGEREUX : log de donnees sensibles en clair
        System.out.println("[VULNERABLE LOG] Paiement recu - Carte : " + cardNumber + " CVV : " + cvv);

        return Map.of(
            "status", "Paiement traite",
            "warning", "Le numero de carte et le CVV sont logues en clair !"
        );
    }

    /**
     * SECURISE : log sans donnees sensibles.
     */
    @Operation(summary = "[SECURISE] Log avec carte masquee",
               description = "Carte masquee (****-****-****-3333), CVV jamais logue. Body: {\"cardNumber\":\"4532111122223333\",\"cvv\":\"123\"}")
    @PostMapping("/secure/log-payment")
    public Map<String, Object> secureLogPayment(@RequestBody Map<String, String> body) {
        String cardNumber = body.get("cardNumber");

        // SECURISE : masquer le numero de carte dans les logs
        String maskedCard = "****-****-****-" + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        System.out.println("[SECURE LOG] Paiement recu - Carte : " + maskedCard);
        // Le CVV n'est JAMAIS logue ni stocke (PCI-DSS)

        return Map.of(
            "status", "Paiement traite",
            "masked_card", maskedCard,
            "info", "Carte masquee dans les logs. CVV jamais logue/stocke (PCI-DSS)."
        );
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***@" + email.substring(atIndex + 1);
        return email.charAt(0) + "****" + email.substring(atIndex);
    }
}
