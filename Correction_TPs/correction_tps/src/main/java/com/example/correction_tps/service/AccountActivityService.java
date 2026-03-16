package com.example.correction_tps.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountActivityService {

    private Map<String, Long> lastActivity = new HashMap<>();
    private Map<String, Boolean> dormantAccounts = new HashMap<>();

    // Durée d'inactivité avant de marquer un compte comme dormant (30 jours)
    private static final long DORMANT_THRESHOLD = 2592000000L;

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
}
