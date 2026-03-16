package com.example.demo_jour_1_spring.service;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PasswordServiceValidation {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private Map<String, String> passwordHashes = new HashMap<>();

    public void updatePassword(String oldPassword, String newPassword, String username) {
            // BCrypt génère automatiquement un sel unique et produit un hash adaptatif
            String hash = passwordEncoder.encode(newPassword);
            passwordHashes.put(username, hash);
    }
}
