package com.example.tpcleancode.service;

import com.example.tpcleancode.dto.LoginRequest;
import com.example.tpcleancode.dto.LoginResponse;
import com.example.tpcleancode.entity.User;
import com.example.tpcleancode.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service d'authentification.
 *
 * CORRECTIONS Clean Code :
 * - La methode doLogin() monolithique est decoupee en methodes specialisees :
 *   validateCredentials(), authenticate(), createSession()
 * - Single Responsibility Principle : chaque methode a une seule responsabilite
 * - Le logging est delegue a SLF4J (pas de System.out.println)
 *
 * CORRECTIONS Securite :
 * - Le mot de passe n'est JAMAIS logge (ni en clair, ni hashé)
 *   AVANT: log.info("Tentative login avec password: " + password);
 * - Message d'erreur generique pour eviter l'enumeration d'utilisateurs
 *   AVANT: "Utilisateur non trouve" vs "Mot de passe incorrect"
 *   APRES: "Identifiants invalides" dans tous les cas
 * - Le hash du mot de passe n'est PAS retourne dans la reponse
 *   AVANT: response.put("hash", user.getPasswordHash());
 * - Token genere avec SecureRandom (voir TokenService)
 * - Utilisation de BCrypt au lieu de MD5
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final String GENERIC_ERROR_MESSAGE = "Identifiants invalides";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Authentifie un utilisateur.
     *
     * @param request le DTO contenant email et mot de passe
     * @return LoginResponse avec les infos non sensibles
     * @throws AuthenticationException si les identifiants sont invalides
     */
    public LoginResponse authenticate(LoginRequest request) {
        // 1. Rechercher l'utilisateur
        Optional<User> optionalUser = findUser(request.getEmail());
        if (optionalUser.isEmpty()) {
            logger.warn("Tentative de connexion echouee pour un email");
            throw new AuthenticationException(GENERIC_ERROR_MESSAGE);
        }

        User user = optionalUser.get();

        // 2. Verifier le mot de passe
        if (!verifyPassword(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Tentative de connexion echouee - mot de passe incorrect");
            throw new AuthenticationException(GENERIC_ERROR_MESSAGE);
        }

        // 3. Verifier que le compte est actif
        if (!user.isActive()) {
            logger.warn("Tentative de connexion sur un compte desactive");
            throw new AuthenticationException(GENERIC_ERROR_MESSAGE);
        }

        // 4. Creer la session
        String token = createSession(user);
        logger.info("Connexion reussie pour l'utilisateur id={}", user.getId());

        // 5. Retourner la reponse SANS donnees sensibles
        return new LoginResponse(user.getEmail(), user.getRole().name(), "Connexion reussie");
    }

    // --- Methodes privees (SRP) ---

    private Optional<User> findUser(String email) {
        return userRepository.findByEmail(email);
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private String createSession(User user) {
        String token = tokenService.generateSessionToken();
        user.setSessionToken(token);
        userRepository.save(user);
        return token;
    }

    /**
     * Exception d'authentification personnalisee.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
