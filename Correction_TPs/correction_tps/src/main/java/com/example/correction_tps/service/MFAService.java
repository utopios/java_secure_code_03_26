package com.example.correction_tps.service;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MFAService {

    private static final int OTP_VALIDITY_DURATION_MS = 300_000; // 5 minutes

    // Stocke l'OTP et son timestamp de création
    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();

    private record OtpEntry(String otp, long createdAt) {}

    public String generateOTP(String username) {
        // Utiliser SecureRandom au lieu de Random pour la sécurité cryptographique
        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));
        otpCache.put(username, new OtpEntry(otp, System.currentTimeMillis()));

        // En production : envoyer par SMS/email via un service dédié
        // Ici on émule l'envoi en affichant dans la console (contexte formation uniquement)
        System.out.println("[MFA] OTP pour " + username + " : " + otp);
        return otp;
    }

    public boolean validateOTP(String username, String inputOtp) {
        OtpEntry entry = otpCache.get(username);
        if (entry == null) {
            return false;
        }

        // Vérifier l'expiration (5 minutes)
        if (System.currentTimeMillis() - entry.createdAt() > OTP_VALIDITY_DURATION_MS) {
            otpCache.remove(username); // Nettoyer l'OTP expiré
            return false;
        }

        // Vérifier le code et supprimer après utilisation (usage unique)
        if (entry.otp().equals(inputOtp)) {
            otpCache.remove(username);
            return true;
        }

        return false;
    }
}
