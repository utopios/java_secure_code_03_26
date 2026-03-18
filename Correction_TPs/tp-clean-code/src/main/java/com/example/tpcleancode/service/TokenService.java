package com.example.tpcleancode.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service de generation de tokens de session.
 *
 * CORRECTION Securite :
 * - Utilise SecureRandom (CSPRNG) au lieu de Random.nextInt(999999)
 * - Genere un token de 256 bits (32 octets) encode en Base64
 * - Token imprevisible et resistant aux attaques par prediction
 *
 * AVANT (vulnerable) :
 *   String token = String.valueOf(new Random().nextInt(999999));
 *   // seulement ~20 bits d'entropie, predictible
 */
@Service
public class TokenService {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Genere un token de session cryptographiquement securise.
     *
     * @return token en Base64 URL-safe (43 caracteres)
     */
    public String generateSessionToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
