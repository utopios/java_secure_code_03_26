package com.example.tpcleancode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service de chiffrement symetrique.
 *
 * CORRECTIONS Securite :
 * - AES-GCM au lieu de AES-ECB (assure confidentialite + authenticite)
 * - IV aleatoire genere a chaque chiffrement (pas de reutilisation)
 * - Cle externalisee dans application.properties (pas codee en dur)
 * - GCM fournit l'integrite des donnees (pas besoin de MAC separe)
 *
 * AVANT (vulnerable) :
 *   - AES/ECB/PKCS5Padding (pas d'IV, patterns detectables)
 *   - Cle codee en dur : "MySuperSecretKey"
 *   - Pas de verification d'integrite
 */
@Service
public class CryptoService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public CryptoService(@Value("${app.crypto.secret-key}") String base64Key, SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("La cle AES doit faire 256 bits (32 octets)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Chiffre une chaine en AES-GCM.
     * Le resultat contient l'IV (12 octets) + le texte chiffre + le tag GCM.
     */
    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            // Concatener IV + texte chiffre
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            logger.error("Erreur lors du chiffrement");
            throw new RuntimeException("Erreur de chiffrement", e);
        }
    }

    /**
     * Dechiffre une chaine chiffree en AES-GCM.
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            logger.error("Erreur lors du dechiffrement");
            throw new RuntimeException("Erreur de dechiffrement", e);
        }
    }
}
