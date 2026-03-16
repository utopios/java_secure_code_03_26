package com.example.correction_tps.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PasswordService {

    //private final  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordEncoder passwordEncoder;

    //Uniquement pour simulation
    private Map<String, String> passwordHashes = new HashMap<>();
    private Map<String, Long> lastPasswordChange = new HashMap<>();
    private Map<String, Long> lastActivity = new HashMap<>();
    private Map<String, Boolean> dormantAccounts = new HashMap<>();

    // Durée d'inactivité avant de marquer un compte comme dormant (30 jours)
    private static final long DORMANT_THRESHOLD = 2592000000L; // 30 jours en millisecondes
    // Durée de validité d'un mot de passe (30 jours)
    private static final long PASSWORD_EXPIRY = 2592000000L;
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchesPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public boolean validatePassword(String password, String username) {
        // Forcer le changement de mot de passe régulièrement
        if (isPasswordExpired(username)) {
            return false;
        }

        // Vérifier si le mot de passe est trop simple ou non conforme à la catégorie
        /*if (!isPasswordComplex(password, category)) {
            return false;
        }*/

        // Si l'utilisateur a un compte dormant
        if (isDormant(username)) {
            return false;
        }

        return true;
    }



    public boolean canRequestPasswordReset(String username) {
        long currentTime = System.currentTimeMillis();
        return !lastPasswordChange.containsKey(username) || (currentTime - lastPasswordChange.get(username)) > 86400000L;
    }

    public boolean isDormant(String username) {
        long currentTime = System.currentTimeMillis();
        return lastActivity.containsKey(username) && (currentTime - lastActivity.get(username)) > DORMANT_THRESHOLD;
    }
    public void markAsDormantIfInactive(String username) {
        if (isDormant(username)) {
            dormantAccounts.put(username, true);
        }
    }

    public void activateAccount(String username) {
        dormantAccounts.put(username, false);
        lastActivity.put(username, System.currentTimeMillis());
    }

    public void updateActivity(String username) {
        lastActivity.put(username, System.currentTimeMillis());
    }

    public boolean validateCurrentPassword(String currentPassword, String username) {
        if (passwordHashes.containsKey(username)) {
            return passwordEncoder.matches(currentPassword, passwordHashes.get(username));
        }
        return false;
    }

    private boolean isPasswordExpired(String username) {
        if (!lastPasswordChange.containsKey(username)) {
            return false; // Premier mot de passe, pas encore expiré
        }
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastPasswordChange.get(username)) > PASSWORD_EXPIRY;
    }

}
