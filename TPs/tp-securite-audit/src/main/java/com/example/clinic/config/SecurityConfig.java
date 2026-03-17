package com.example.clinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //Manque un PasswordEncoder => CRITIQUE
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Misconfiguration => ELEVE
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Controle d'accès mal configuré => CRITIQUE
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            //Misconfiguration => Moyen
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
