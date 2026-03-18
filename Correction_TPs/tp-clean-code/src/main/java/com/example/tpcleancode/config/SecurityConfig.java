package com.example.tpcleancode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Configuration de securite Spring Security.
 *
 * CORRECTIONS Securite :
 *
 * 1. CSRF etait desactive
 *    AVANT: http.csrf().disable()
 *    APRES: CSRF active (protection par defaut de Spring Security)
 *
 * 2. frameOptions etait desactive
 *    AVANT: http.headers().frameOptions().disable()
 *    APRES: frameOptions DENY (empeche le clickjacking)
 *
 * 3. Tout etait en permitAll
 *    AVANT: http.authorizeHttpRequests().anyRequest().permitAll()
 *    APRES: seul /api/auth/** est public, le reste necessite une authentification
 *
 * 4. En-tetes de securite supplementaires :
 *    - Content-Security-Policy: default-src 'self'
 *    - X-Content-Type-Options: nosniff (par defaut Spring)
 *    - Strict-Transport-Security (HSTS)
 *    - Referrer-Policy: strict-origin-when-cross-origin
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Active @PreAuthorize sur les controllers
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF active (protection contre les attaques CSRF)
                // Pour une API REST pure avec tokens, on peut le desactiver
                // mais ici on montre la bonne pratique avec sessions
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/auth/**") // login n'a pas de session
                )

                // En-tetes de securite
                .headers(headers -> headers
                        // Clickjacking : empeche l'inclusion dans un iframe
                        .frameOptions(frame -> frame.deny())
                        // CSP : limite les sources de contenu
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'"))
                        // Referrer-Policy
                        .referrerPolicy(referrer ->
                                referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )

                // Regles d'autorisation
                .authorizeHttpRequests(auth -> auth
                        // Seul le endpoint de login est public
                        .requestMatchers("/api/auth/**").permitAll()
                        // Tout le reste necessite une authentification
                        .anyRequest().authenticated()
                )

                // HTTP Basic pour simplifier le TP (en production : JWT ou OAuth2)
                .httpBasic(basic -> {});

        return http.build();
    }

    /**
     * BCrypt au lieu de MD5.
     *
     * CORRECTION Securite :
     * - MD5 est casse et ne doit JAMAIS etre utilise pour hasher des mots de passe
     * - BCrypt inclut un sel aleatoire automatiquement
     * - BCrypt a un facteur de cout configurable (12 par defaut ici)
     *
     * AVANT (DataInitializer) :
     *   MessageDigest.getInstance("MD5").digest(password.getBytes());
     *   // Pas de sel, hash rapide = vulnerable aux rainbow tables
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
