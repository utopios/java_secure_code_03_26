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
        partie2_ECB_PingouinDemo();
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
