package com.example.correction_tps.validator;


import org.springframework.stereotype.Component;

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

    public boolean validatePassword(String password, String level) {
        if (containsDictionaryWord(password) || containsSimpleSequence(password)) {
            return false;
        }

        return switch (level) {
            case "important" -> Pattern.matches(SIMPLE_PATTERN, password);
            case "critical" -> Pattern.matches(CRITICAL_PATTERN, password);
            case "highly_critical" -> Pattern.matches(HIGHLY_CRITICAL_PATTERN, password);
            default -> false;
        };
    }

    private static boolean containsSimpleSequence(String password) {
        String lower = password.toLowerCase();
        // Vérifie les séquences numériques (123, 234, etc.), répétitions (aaa, 111), et séquences clavier
        return lower.matches(".*(012|123|234|345|456|567|678|789|890).*")
                || lower.matches(".*(.)\\1{2,}.*") // 3+ caractères identiques consécutifs
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
