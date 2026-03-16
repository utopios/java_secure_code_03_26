package com.example.correction_tps.validator;


import com.example.correction_tps.service.AccountActivityService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {
    private static final String SIMPLE_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$";
    private static final String CRITICAL_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()\\-_=+])[A-Za-z\\d@$!%*?&#^()\\-_=+]{8,}$";
    private static final String HIGHLY_CRITICAL_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()\\-_=+])[A-Za-z\\d@$!%*?&#^()\\-_=+]{14,}$";
    //Un service externe permettra de vérifier si les mots de passe sont dans un dictionnaire.
    private static final String[] DICTIONARY_WORDS = {
            "password", "123456", "qwerty", "admin", "letmein", "welcome",
            "monkey", "dragon", "master", "login", "abc123", "azerty"
    };

    // Durée de validité d'un mot de passe (30 jours)
    private static final long PASSWORD_EXPIRY = 2592000000L;

    private final AccountActivityService accountActivityService;

    //Uniquement pour simulation
    private Map<String, Long> lastPasswordChange = new HashMap<>();

    public PasswordValidator(AccountActivityService accountActivityService) {
        this.accountActivityService = accountActivityService;
    }

    public boolean validatePassword(String password, String username, String level) {
        if (isPasswordExpired(username)) {
            return false;
        }

        if (containsDictionaryWord(password) || containsSimpleSequence(password)) {
            return false;
        }

        if (accountActivityService.isDormant(username)) {
            return false;
        }

        return switch (level) {
            case "important" -> Pattern.matches(SIMPLE_PATTERN, password);
            case "critical" -> Pattern.matches(CRITICAL_PATTERN, password);
            case "highly_critical" -> Pattern.matches(HIGHLY_CRITICAL_PATTERN, password);
            default -> false;
        };
    }

    public boolean canRequestPasswordReset(String username) {
        long currentTime = System.currentTimeMillis();
        return !lastPasswordChange.containsKey(username) || (currentTime - lastPasswordChange.get(username)) > 86400000L;
    }

    private boolean isPasswordExpired(String username) {
        if (!lastPasswordChange.containsKey(username)) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastPasswordChange.get(username)) > PASSWORD_EXPIRY;
    }

    private static boolean containsSimpleSequence(String password) {
        String lower = password.toLowerCase();
        return lower.matches(".*(012|123|234|345|456|567|678|789|890).*")
                || lower.matches(".*(.)\\1{2,}.*")
                || lower.matches(".*(abc|bcd|cde|def|qwerty|azerty).*");
    }

    private static boolean containsDictionaryWord(String password) {
        String lower = password.toLowerCase();
        for (String word : DICTIONARY_WORDS) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
