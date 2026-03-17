package com.example.demo_jour_2.demo;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Arrays;

public class CryptographyFullDemo {

    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 250;
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int RSA_KEY_SIZE = 2048;

    public static void main(String[] args) throws Exception {
        //partie1_SymetriqueVsAsymetrique();
        //partie2_ECB_PingouinDemo();
        //partie3_AES_GCM();
        partie4_IV_Role_Et_Unicite();
    }

    private static void partie1_SymetriqueVsAsymetrique() throws Exception {
        printSection("PARTIE 1 : SYMETRIQUE VS ASYMETRIQUE - QUAND UTILISER QUOI");

        String message = "Donnees confidentielles de l'entreprise";

        // --- Chiffrement symetrique : une seule cle ---
        System.out.println("--- SYMETRIQUE (AES) ---");
        System.out.println("  - Une seule cle pour chiffrer ET dechiffrer");
        System.out.println("  - Tres rapide (operations bit-a-bit)");
        System.out.println("  - Probleme : comment transmettre la cle a l'autre partie ?");
        System.out.println();

        KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
        aesKeyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = aesKeyGen.generateKey();

        long startAes = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            c.doFinal(message.getBytes());
        }
        long durationAes = System.nanoTime() - startAes;

        System.out.println("  Benchmark : 10 000 chiffrements AES-256-GCM");
        System.out.println("  Duree     : " + (durationAes / 1_000_000) + " ms");
        System.out.println();

        // --- Chiffrement asymetrique : deux cles ---
        System.out.println("--- ASYMETRIQUE (RSA) ---");
        System.out.println("  - Deux cles : publique (chiffre) et privee (dechiffre)");
        System.out.println("  - Beaucoup plus lent (~1000x)");
        System.out.println("  - Avantage : pas besoin de partager de secret");
        System.out.println();

        KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
        rsaKeyGen.initialize(RSA_KEY_SIZE);
        KeyPair rsaKeyPair = rsaKeyGen.generateKeyPair();

        long startRsa = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            c.init(Cipher.ENCRYPT_MODE, rsaKeyPair.getPublic());
            c.doFinal(message.getBytes());
        }
        long durationRsa = System.nanoTime() - startRsa;

        System.out.println("  Benchmark : 1 000 chiffrements RSA-2048-OAEP (10x moins !)");
        System.out.println("  Duree     : " + (durationRsa / 1_000_000) + " ms");
        System.out.println();

        double ratio = ((double) durationRsa * 10) / durationAes;
        System.out.println("  --> RSA est environ " + String.format("%.0f", ratio) + "x plus lent qu'AES");
        System.out.println("  --> C'est pourquoi on utilise le CHIFFREMENT HYBRIDE (Partie 6)");
        System.out.println();

        System.out.println("  REGLE D'OR :");
        System.out.println("    - AES  = chiffrer les DONNEES (volumes importants, rapide)");
        System.out.println("    - RSA  = chiffrer les CLES ou SIGNER (quelques octets)");
        System.out.println("    - On ne chiffre JAMAIS des donnees volumineuses avec RSA !");
        System.out.println();
    }

    private static void partie2_ECB_PingouinDemo() throws Exception {
        printSection("PARTIE 2 : AES-ECB - POURQUOI C'EST INTERDIT (DEMO DU PINGOUIN)");

        System.out.println("  ECB = Electronic CodeBook");
        System.out.println("  Chaque bloc de 16 octets est chiffre INDEPENDAMMENT.");
        System.out.println("  Consequence : blocs identiques en clair = blocs identiques chiffres.");
        System.out.println("  Les PATTERNS de l'image originale sont donc PRESERVES.");
        System.out.println();

        // Generer l'image du pingouin
        BufferedImage tuxImage = drawTux();
        ImageIO.write(tuxImage, "bmp", new File("tux_original.bmp"));
        System.out.println("  [1] Image originale generee : tux_original.bmp");

        // Extraire les pixels bruts
        byte[] rawPixels = extractRawPixels(tuxImage);

        // Generer une cle AES
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey key = keyGen.generateKey();

        // --- ECB : MAUVAIS ---
        byte[] ecbEncrypted = encryptECB(rawPixels, key);
        BufferedImage ecbImage = rebuildImage(ecbEncrypted, IMAGE_WIDTH, IMAGE_HEIGHT);
        ImageIO.write(ecbImage, "bmp", new File("tux_ecb_encrypted.bmp"));
        System.out.println("  [2] AES/ECB chiffre         : tux_ecb_encrypted.bmp");
        System.out.println("      --> OUVREZ LE FICHIER : la silhouette du pingouin est VISIBLE !");
        System.out.println();

        // --- CBC : BON ---
        byte[] cbcEncrypted = encryptCBC(rawPixels, key);
        BufferedImage cbcImage = rebuildImage(cbcEncrypted, IMAGE_WIDTH, IMAGE_HEIGHT);
        ImageIO.write(cbcImage, "bmp", new File("tux_cbc_encrypted.bmp"));
        System.out.println("  [3] AES/CBC chiffre         : tux_cbc_encrypted.bmp");
        System.out.println("      --> Bruit total, aucun pattern visible.");
        System.out.println();

        // Demo textuelle : meme texte chiffre deux fois
        System.out.println("  --- Demonstration textuelle ---");
        String block16 = "AAAAAAAAAAAAAAAA"; // exactement 16 octets

        Cipher ecbCipher = Cipher.getInstance("AES/ECB/NoPadding");
        ecbCipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted1 = ecbCipher.doFinal(block16.getBytes());
        byte[] encrypted2 = ecbCipher.doFinal(block16.getBytes()); // meme entree

        System.out.println("  Bloc en clair : \"" + block16 + "\" (16 octets)");
        System.out.println("  ECB chiffrement #1 : " + bytesToHex(encrypted1));
        System.out.println("  ECB chiffrement #2 : " + bytesToHex(encrypted2));
        System.out.println("  Identiques ?       : " + Arrays.equals(encrypted1, encrypted2));
        System.out.println("  --> OUI ! Meme entree = meme sortie. C'est le probleme fondamental.");
        System.out.println();

        System.out.println("  PIEGE JAVA :");
        System.out.println("    Cipher.getInstance(\"AES\")  --> utilise ECB par DEFAUT !");
        System.out.println("    C'est pour cela que SonarQube le flag systematiquement.");
        System.out.println();
    }

    private static void partie3_AES_GCM() throws Exception {
        printSection("PARTIE 3 : AES-GCM - CHIFFREMENT AUTHENTIFIE (CONFIDENTIALITE + INTEGRITE)");

        System.out.println("  GCM = Galois/Counter Mode");
        System.out.println("  Fournit 3 garanties en UNE seule operation (AEAD) :");
        System.out.println("    1. Confidentialite - les donnees sont illisibles sans la cle");
        System.out.println("    2. Integrite       - toute modification est detectee");
        System.out.println("    3. Authenticite    - seul le detenteur de la cle a pu produire ce chiffre");
        System.out.println();

        // Generer une cle AES-256
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        SecretKey key = keyGen.generateKey();
        System.out.println("  Cle AES-" + AES_KEY_SIZE + " generee : " + bytesToHex(key.getEncoded()).substring(0, 32) + "...");

        // Generer un IV de 12 octets
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        System.out.println("  IV (12 octets)    : " + bytesToHex(iv));
        System.out.println();

        // --- Chiffrement ---
        String plaintext = "Numero de carte bancaire : 4532-XXXX-XXXX-1234";
        System.out.println("  Texte en clair : \"" + plaintext + "\"");
        System.out.println();

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // AAD = Associated Authenticated Data (en clair, mais authentifie)
        String aad = "userId=42;operation=payment";
        cipher.updateAAD(aad.getBytes());
        System.out.println("  AAD (Associated Authenticated Data) : \"" + aad + "\"");
        System.out.println("  --> Ces donnees ne sont PAS chiffrees, mais leur integrite est verifiee.");
        System.out.println();

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        System.out.println("  Chiffre (hex) : " + bytesToHex(ciphertext));
        System.out.println("  Taille clair  : " + plaintext.getBytes().length + " octets");
        System.out.println("  Taille chiffre: " + ciphertext.length + " octets (+" + GCM_TAG_LENGTH / 8 + " octets pour le tag)");
        System.out.println();

        // --- Dechiffrement normal ---
        System.out.println("  --- Dechiffrement normal ---");
        Cipher decryptCipher = Cipher.getInstance("AES/GCM/NoPadding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        decryptCipher.updateAAD(aad.getBytes()); // meme AAD obligatoire
        byte[] decrypted = decryptCipher.doFinal(ciphertext);
        System.out.println("  Resultat : \"" + new String(decrypted) + "\"");
        System.out.println();

        // --- Demo de l'integrite : alterer 1 octet ---
        System.out.println("  --- Demo de l'integrite : alteration d'1 seul octet ---");
        byte[] tampered = ciphertext.clone();
        tampered[0] ^= 0x01; // alterer 1 seul bit

        try {
            Cipher tamperCipher = Cipher.getInstance("AES/GCM/NoPadding");
            tamperCipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            tamperCipher.updateAAD(aad.getBytes());
            tamperCipher.doFinal(tampered);
            System.out.println("  ERREUR : le dechiffrement n'aurait pas du reussir !");
        } catch (javax.crypto.AEADBadTagException e) {
            System.out.println("  Exception levee : " + e.getClass().getSimpleName());
            System.out.println("  --> GCM a detecte l'alteration ! Les donnees sont rejetees.");
            System.out.println("  --> Avec CBC simple (sans HMAC), cette alteration passerait inapercue.");
        }
        System.out.println();

        // --- Demo de l'AAD : modifier l'AAD ---
        System.out.println("  --- Demo de l'AAD : modifier les donnees associees ---");
        try {
            Cipher aadCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aadCipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            aadCipher.updateAAD("userId=99;operation=payment".getBytes()); // AAD modifie !
            aadCipher.doFinal(ciphertext);
            System.out.println("  ERREUR : le dechiffrement n'aurait pas du reussir !");
        } catch (javax.crypto.AEADBadTagException e) {
            System.out.println("  Exception levee : " + e.getClass().getSimpleName());
            System.out.println("  --> L'attaquant a tente de changer userId de 42 a 99.");
            System.out.println("  --> GCM l'a detecte : l'AAD fait partie du calcul du tag.");
            System.out.println("  --> Cas d'usage : empecher un utilisateur de rejouer un paiement");
            System.out.println("      sur un autre compte en modifiant le header.");
        }
        System.out.println();
    }

    private static void partie4_IV_Role_Et_Unicite() throws Exception {
        printSection("PARTIE 4 : IV (INITIALIZATION VECTOR) - ROLE, TAILLE, UNICITE");

        System.out.println("  L'IV garantit que chiffrer le MEME message deux fois avec la MEME cle");
        System.out.println("  produit deux resultats DIFFERENTS.");
        System.out.println();
        System.out.println("  Specifications pour AES-GCM :");
        System.out.println("    - Taille          : 12 octets (96 bits) - recommandation NIST");
        System.out.println("    - Unicite         : OBLIGATOIRE - ne jamais reutiliser avec la meme cle");
        System.out.println("    - Confidentialite : PAS necessaire - peut etre transmis en clair");
        System.out.println();

        // Generer une cle
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        SecretKey key = keyGen.generateKey();

        String message = "Message secret identique";

        // --- Demo : meme message, meme cle, IV differents ---
        System.out.println("  --- Meme message, meme cle, IV differents ---");
        System.out.println("  Message : \"" + message + "\"");
        System.out.println();

        for (int i = 1; i <= 3; i++) {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(message.getBytes());

            System.out.println("  Chiffrement #" + i + " :");
            System.out.println("    IV      : " + bytesToHex(iv));
            System.out.println("    Chiffre : " + bytesToHex(encrypted).substring(0, 40) + "...");
        }
        System.out.println("  --> Trois resultats DIFFERENTS grace aux IV differents.");
        System.out.println("  --> Sans IV, un attaquant verrait que c'est le meme message.");
        System.out.println();

        // --- Pourquoi exactement 12 octets ? ---
        System.out.println("  --- Pourquoi exactement 12 octets pour GCM ? ---");
        System.out.println("  - 12 octets est la taille OPTIMISEE : l'IV est directement utilise");
        System.out.println("    comme valeur initiale du compteur CTR.");
        System.out.println("  - Avec une autre taille, GCM doit d'abord hasher l'IV via GHASH,");
        System.out.println("    ce qui est plus lent et legerement moins sur.");
        System.out.println("  - Avec 12 octets et SecureRandom, la probabilite de collision");
        System.out.println("    est negligeable pour ~2^48 messages (des milliards de milliards).");
        System.out.println();

        // --- DANGER : reutilisation d'IV ---
        System.out.println("  --- DANGER : Reutilisation du meme IV avec la meme cle ---");
        System.out.println();

        byte[] fixedIv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(fixedIv);

        String message1 = "Transfert : 1000 EUR";
        String message2 = "Transfert : 9999 EUR";

        // Chiffrer les deux messages avec le MEME IV (INTERDIT !)
        Cipher c1 = Cipher.getInstance("AES/GCM/NoPadding");
        c1.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, fixedIv));
        byte[] enc1 = c1.doFinal(message1.getBytes());

        Cipher c2 = Cipher.getInstance("AES/GCM/NoPadding");
        c2.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, fixedIv));
        byte[] enc2 = c2.doFinal(message2.getBytes());

        // XOR des deux chiffres = XOR des deux clairs !
        System.out.println("  Message 1 : \"" + message1 + "\"");
        System.out.println("  Message 2 : \"" + message2 + "\"");
        System.out.println("  MEME IV utilise pour les deux : " + bytesToHex(fixedIv));
        System.out.println();

        int minLen = Math.min(enc1.length, enc2.length);
        minLen = Math.min(minLen, Math.min(message1.length(), message2.length()));
        byte[] xorCiphertexts = new byte[minLen];
        byte[] xorPlaintexts = new byte[minLen];

        for (int i = 0; i < minLen; i++) {
            xorCiphertexts[i] = (byte) (enc1[i] ^ enc2[i]);
            xorPlaintexts[i] = (byte) (message1.getBytes()[i] ^ message2.getBytes()[i]);
        }

        System.out.println("  XOR des chiffres  : " + bytesToHex(xorCiphertexts));
        System.out.println("  XOR des clairs    : " + bytesToHex(xorPlaintexts));
        System.out.println("  Identiques ?      : " + Arrays.equals(xorCiphertexts, xorPlaintexts));
        System.out.println();
        System.out.println("  --> Le XOR des chiffres REVELE le XOR des clairs !");
        System.out.println("  --> Si l'attaquant connait un des deux messages (known-plaintext),");
        System.out.println("      il peut retrouver l'autre par simple XOR.");
        System.out.println("  --> C'est EXACTEMENT le bug qui a casse le chiffrement de la PS3");
        System.out.println("      par Sony en 2010 (reutilisation du meme 'random' pour ECDSA).");
        System.out.println();

        // --- Bonne pratique : generation de l'IV ---
        System.out.println("  --- Bonne pratique Java ---");
        System.out.println("  // CORRECT : toujours utiliser SecureRandom");
        System.out.println("  byte[] iv = new byte[12];");
        System.out.println("  new SecureRandom().nextBytes(iv);");
        System.out.println();
        System.out.println("  // INTERDIT : Random n'est pas cryptographiquement sur");
        System.out.println("  // new Random().nextBytes(iv);  --> predictible !");
        System.out.println();
        System.out.println("  // STOCKAGE : IV transmis en clair, concatene devant le chiffre");
        System.out.println("  // [IV: 12 octets][Ciphertext][Tag: 16 octets]");
        System.out.println();
    }

    private static byte[] encryptCBC(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        byte[] cbcIv = new byte[16];
        new SecureRandom().nextBytes(cbcIv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(cbcIv));
        int len = (data.length / 16) * 16;
        byte[] input = new byte[len];
        System.arraycopy(data, 0, input, 0, len);
        return cipher.doFinal(input);
    }

    private static byte[] encryptECB(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        int len = (data.length / 16) * 16;
        byte[] input = new byte[len];
        System.arraycopy(data, 0, input, 0, len);
        return cipher.doFinal(input);
    }


    private static BufferedImage drawTux() {
        BufferedImage img = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        g.setColor(Color.BLACK);
        g.fillOval(40, 30, 120, 180);

        g.setColor(Color.WHITE);
        g.fillOval(65, 70, 70, 120);

        g.setColor(Color.BLACK);
        g.fillOval(55, 15, 90, 80);

        g.setColor(Color.WHITE);
        g.fillOval(72, 35, 22, 22);
        g.fillOval(106, 35, 22, 22);

        g.setColor(Color.BLACK);
        g.fillOval(80, 42, 10, 10);
        g.fillOval(114, 42, 10, 10);

        g.setColor(new Color(255, 165, 0));
        g.fillPolygon(new int[]{85, 100, 115}, new int[]{58, 78, 58}, 3);

        g.setColor(new Color(255, 165, 0));
        g.fillOval(55, 200, 40, 15);
        g.fillOval(105, 200, 40, 15);

        g.setColor(Color.BLACK);
        g.fillOval(25, 80, 35, 100);
        g.fillOval(140, 80, 35, 100);

        g.dispose();
        return img;
    }

    /** Extrait les pixels bruts RGB d'une image. */
    private static byte[] extractRawPixels(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        byte[] pixels = new byte[w * h * 3];
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                pixels[idx++] = (byte) ((rgb >> 16) & 0xFF);
                pixels[idx++] = (byte) ((rgb >> 8) & 0xFF);
                pixels[idx++] = (byte) (rgb & 0xFF);
            }
        }
        return pixels;
    }

    /** Reconstruit une image a partir de pixels chiffres. */
    private static BufferedImage rebuildImage(byte[] encryptedPixels, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (idx + 2 < encryptedPixels.length) {
                    int r = encryptedPixels[idx++] & 0xFF;
                    int g = encryptedPixels[idx++] & 0xFF;
                    int b = encryptedPixels[idx++] & 0xFF;
                    img.setRGB(x, y, (r << 16) | (g << 8) | b);
                }
            }
        }
        return img;
    }

    /** Convertit un tableau d'octets en chaine hexadecimale. */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** Affiche un bandeau de titre principal. */
    private static void printBanner(String title) {
        System.out.println();
        System.out.println("=".repeat(76));
        System.out.println("  " + title);
        System.out.println("=".repeat(76));
        System.out.println();
    }


    private static void printSection(String title) {
        System.out.println();
        System.out.println("-".repeat(76));
        System.out.println("  " + title);
        System.out.println("-".repeat(76));
        System.out.println();
    }
}
