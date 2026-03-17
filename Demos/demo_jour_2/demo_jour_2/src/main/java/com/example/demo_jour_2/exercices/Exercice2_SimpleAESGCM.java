package com.example.demo_jour_2.exercices;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class Exercice2_SimpleAESGCM {

    private static final int GCM_IV_LENGTH = 12;     // 96 bits - recommandation NIST
    private static final int GCM_TAG_LENGTH = 128;   // 128 bits - tag d'authentification

    /**
     * Chiffre un message avec AES-GCM.
     *
     * Format de sortie : [IV (12 octets)][ciphertext + tag GCM]
     */
    public byte[] encrypt(String plaintext, SecretKey key) throws Exception {
        // 1. Generer un IV aleatoire de 12 octets
        //    SecureRandom est obligatoire (pas Random !)
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // 2. Initialiser le Cipher en mode GCM
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // 3. Chiffrer les donnees
        //    doFinal retourne ciphertext + tag GCM (16 octets) concatenes
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

        // 4. Concatener IV + ciphertext pour le transport
        //    Le destinataire a besoin de l'IV pour dechiffrer
        //    L'IV n'a pas besoin d'etre secret, juste unique
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

        return result;
    }

    /**
     * Dechiffre un message chiffre par encrypt().
     *
     * Attend le format : [IV (12 octets)][ciphertext + tag GCM]
     */
    public String decrypt(byte[] ciphertextWithIv, SecretKey key) throws Exception {
        // 1. Extraire l'IV (les 12 premiers octets)
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(ciphertextWithIv, 0, iv, 0, GCM_IV_LENGTH);

        // 2. Extraire le ciphertext (tout le reste)
        byte[] ciphertext = new byte[ciphertextWithIv.length - GCM_IV_LENGTH];
        System.arraycopy(ciphertextWithIv, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        // 3. Initialiser le Cipher en mode dechiffrement avec le meme IV
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // 4. Dechiffrer
        //    doFinal verifie aussi le tag GCM (integrite)
        //    Si les donnees ont ete alterees --> AEADBadTagException
        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext);
    }

    // ========================================================================
    //  TESTS
    // ========================================================================

    public static void main(String[] args) throws Exception {
        System.out.println("=== EXERCICE 2 - SOLUTION : AES-GCM basique ===");
        System.out.println();

        // Generer une cle AES-256
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();

        Exercice2_SimpleAESGCM crypto = new Exercice2_SimpleAESGCM();
        String message = "Donnees confidentielles";

        // Chiffrer
        byte[] encrypted = crypto.encrypt(message, key);
        System.out.println("Taille du message chiffre : " + encrypted.length + " octets");
        System.out.println("  = 12 (IV) + " + message.getBytes().length + " (donnees) + 16 (tag GCM)");
        System.out.println();

        // Dechiffrer
        String decrypted = crypto.decrypt(encrypted, key);

        // Verifier
        System.out.println("Original  : " + message);
        System.out.println("Dechiffre : " + decrypted);
        System.out.println("Identique : " + message.equals(decrypted));
        System.out.println();

        // Verifier que deux chiffrements donnent des resultats differents
        // (grace aux IV differents)
        byte[] encrypted2 = crypto.encrypt(message, key);
        System.out.println("Chiffres differents : " + !Arrays.equals(encrypted, encrypted2));
        System.out.println("  --> C'est grace aux IV aleatoires differents.");
        System.out.println("  --> Un attaquant ne peut pas savoir que c'est le meme message.");
    }
}
