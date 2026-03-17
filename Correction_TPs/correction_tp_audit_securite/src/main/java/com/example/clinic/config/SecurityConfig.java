package com.example.clinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CORRIGE #6 : Bean PasswordEncoder (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORRIGE #1 : CSRF desactive car API REST stateless (pas de cookies de session)
            // Si l'app utilisait des sessions/cookies, il faudrait le reactiver
            .csrf(csrf -> csrf.disable())

            // CORRIGE #2 + #3 + #24 : Controle d'acces par role
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/patients/**", "/api/prescriptions/**").hasAnyRole("ADMIN", "DOCTOR")
                .anyRequest().authenticated()
            )

            // CORRIGE #4 + #5 : Headers de securite
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'"))
                .httpStrictTransportSecurity(hsts ->
                    hsts.maxAgeInSeconds(31536000).includeSubDomains(true))
            );

        return http.build();
    }
}
