package com.example.correction_tps.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TP2 - Limitation des transactions par utilisateur avec Bucket4j.
 *
 * Opérations normales : 10 transactions toutes les 10 minutes.
 * Opérations critiques (paiements, modifications majeures) : 5 transactions par heure.
 */
@Service
public class RateLimitingService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingService.class);

    private final Map<String, Bucket> normalBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> criticalBuckets = new ConcurrentHashMap<>();

    private final AttackDetectionService attackDetectionService;

    public RateLimitingService(AttackDetectionService attackDetectionService) {
        this.attackDetectionService = attackDetectionService;
    }

    /**
     * Vérifie si l'utilisateur peut effectuer une opération normale.
     */
    public boolean allowNormalOperation(String username) {
        Bucket bucket = normalBuckets.computeIfAbsent(username, this::createNormalBucket);
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            logger.warn("Rate limit atteint pour les opérations normales - utilisateur: {}", sanitize(username));
            attackDetectionService.recordExcessiveTransaction(username);
        }
        return allowed;
    }

    /**
     * Vérifie si l'utilisateur peut effectuer une opération critique.
     */
    public boolean allowCriticalOperation(String username) {
        Bucket bucket = criticalBuckets.computeIfAbsent(username, this::createCriticalBucket);
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            logger.warn("Rate limit atteint pour les opérations critiques - utilisateur: {}", sanitize(username));
            attackDetectionService.recordExcessiveTransaction(username);
        }
        return allowed;
    }

    private Bucket createNormalBucket(String username) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(10)))
                .build();
    }

    private Bucket createCriticalBucket(String username) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofHours(1)))
                .build();
    }

    private String sanitize(String input) {
        return input.replaceAll("[\\r\\n]", "");
    }
}
