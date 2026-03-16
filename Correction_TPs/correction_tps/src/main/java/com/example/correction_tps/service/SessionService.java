package com.example.correction_tps.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * TP2 - Gestion des sessions selon le niveau de criticité.
 *
 * Session normale : 1 heure (3600s)
 * Session critique (transactionnelle) : 20 minutes (1200s)
 * Session hautement critique : 5 minutes (300s)
 */
@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private static final int NORMAL_SESSION_TIMEOUT = 3600;       // 1 heure
    private static final int CRITICAL_SESSION_TIMEOUT = 1200;     // 20 minutes
    private static final int HIGHLY_CRITICAL_SESSION_TIMEOUT = 300; // 5 minutes

    /**
     * Configure la durée de session selon le niveau de criticité.
     */
    public void configureSessionTimeout(HttpServletRequest request, String level) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        int timeout = switch (level) {
            case "critical" -> CRITICAL_SESSION_TIMEOUT;
            case "highly_critical" -> HIGHLY_CRITICAL_SESSION_TIMEOUT;
            default -> NORMAL_SESSION_TIMEOUT;
        };

        session.setMaxInactiveInterval(timeout);
        logger.info("Session configurée avec timeout {}s pour le niveau: {}", timeout, level);
    }

    /**
     * Renouvelle l'ID de session (protection contre le session fixation).
     * À appeler après un changement de rôle ou de privilège.
     */
    public void renewSession(HttpServletRequest request) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        // Crée une nouvelle session avec un nouvel ID
        request.getSession(true);
        logger.info("ID de session renouvelé");
    }

    /**
     * Invalide proprement la session (logout).
     */
    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
