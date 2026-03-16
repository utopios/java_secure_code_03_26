package com.example.correction_tps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TP2 - Détection des attaques et comportements suspects.
 *
 * Détecte :
 * - Connexion simultanée depuis différentes IPs pour un même utilisateur
 * - Tentatives répétées de transactions au-delà des limites
 * - Tentatives répétées de modification d'informations sensibles
 *
 * Les logs ne contiennent aucune donnée sensible (mots de passe, tokens, etc.).
 */
@Service
public class AttackDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(AttackDetectionService.class);

    private static final int MAX_IPS_PER_USER = 3;
    private static final int MAX_SENSITIVE_MODIFICATION_ATTEMPTS = 5;
    private static final int MAX_EXCESSIVE_TRANSACTIONS = 10;

    // IP de connexion par utilisateur
    private final Map<String, Set<String>> userIps = new ConcurrentHashMap<>();

    // Compteur de tentatives de modification d'infos sensibles
    private final Map<String, AtomicInteger> sensitiveModificationAttempts = new ConcurrentHashMap<>();

    // Compteur de dépassements de rate limit
    private final Map<String, AtomicInteger> excessiveTransactionAttempts = new ConcurrentHashMap<>();

    /**
     * Enregistre une IP de connexion pour un utilisateur.
     * Détecte les connexions depuis trop d'IPs différentes.
     */
    public void recordLoginIp(String username, String ipAddress) {
        Set<String> ips = userIps.computeIfAbsent(username, k -> new CopyOnWriteArraySet<>());
        ips.add(ipAddress);

        if (ips.size() > MAX_IPS_PER_USER) {
            logger.warn("ALERTE SECURITE - Connexions depuis {} IPs différentes pour l'utilisateur: {}",
                    ips.size(), sanitize(username));
        }
    }

    /**
     * Vérifie si un utilisateur a des connexions suspectes (multiples IPs).
     */
    public boolean hasSuspiciousMultipleIps(String username) {
        Set<String> ips = userIps.get(username);
        return ips != null && ips.size() > MAX_IPS_PER_USER;
    }

    /**
     * Enregistre une tentative de modification d'information sensible.
     */
    public void recordSensitiveModificationAttempt(String username) {
        AtomicInteger count = sensitiveModificationAttempts
                .computeIfAbsent(username, k -> new AtomicInteger(0));
        int attempts = count.incrementAndGet();

        if (attempts > MAX_SENSITIVE_MODIFICATION_ATTEMPTS) {
            logger.warn("ALERTE SECURITE - {} tentatives de modification sensible pour l'utilisateur: {}",
                    attempts, sanitize(username));
        }
    }

    /**
     * Vérifie si l'utilisateur a dépassé le seuil de modifications sensibles.
     */
    public boolean hasExcessiveSensitiveModifications(String username) {
        AtomicInteger count = sensitiveModificationAttempts.get(username);
        return count != null && count.get() > MAX_SENSITIVE_MODIFICATION_ATTEMPTS;
    }

    /**
     * Enregistre un dépassement de rate limit (appelé par RateLimitingService).
     */
    public void recordExcessiveTransaction(String username) {
        AtomicInteger count = excessiveTransactionAttempts
                .computeIfAbsent(username, k -> new AtomicInteger(0));
        int attempts = count.incrementAndGet();

        if (attempts > MAX_EXCESSIVE_TRANSACTIONS) {
            logger.warn("ALERTE SECURITE - {} dépassements de rate limit pour l'utilisateur: {}",
                    attempts, sanitize(username));
        }
    }

    /**
     * Vérifie si l'utilisateur a un comportement suspect global.
     */
    public boolean isSuspicious(String username) {
        return hasSuspiciousMultipleIps(username)
                || hasExcessiveSensitiveModifications(username)
                || hasExcessiveTransactions(username);
    }

    /**
     * Réinitialise les compteurs pour un utilisateur (après un délai ou action admin).
     */
    public void resetCounters(String username) {
        userIps.remove(username);
        sensitiveModificationAttempts.remove(username);
        excessiveTransactionAttempts.remove(username);
    }

    private boolean hasExcessiveTransactions(String username) {
        AtomicInteger count = excessiveTransactionAttempts.get(username);
        return count != null && count.get() > MAX_EXCESSIVE_TRANSACTIONS;
    }

    private String sanitize(String input) {
        return input.replaceAll("[\\r\\n]", "");
    }
}
