package com.example.correction_tps.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    //Uniquement pour simulation
    private Map<String, String> passwordHashes = new HashMap<>();

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchesPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public boolean validateCurrentPassword(String currentPassword, String username) {
        if (passwordHashes.containsKey(username)) {
            return passwordEncoder.matches(currentPassword, passwordHashes.get(username));
        }
        return false;
    }
}
