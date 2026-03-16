package com.example.correction_tps.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // --- RBAC : contrôle d'accès basé sur les rôles ---
                // --- Principe du moindre privilège : tout est interdit par défaut ---
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/superadmin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/transactions/critical/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/transactions/**").authenticated()
                        .requestMatchers("/api/account/**").authenticated()
                        .anyRequest().denyAll()
                )

                // --- Protection CSRF ---
                // CookieCsrfTokenRepository pour exposer le token dans un cookie lisible par le front
                // Endpoints publics (login, register) exclus via le permitAll ci-dessus
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/auth/login", "/api/auth/register")
                )

                // --- Gestion des sessions ---
                .sessionManagement(session -> session
                        // Protection contre le session fixation :
                        // Renouvelle l'ID de session après authentification
                        .sessionFixation(fix -> fix.migrateSession())
                        // Limite à 1 session simultanée par utilisateur
                        // L'ancienne session est invalidée (maxSessionsPreventsLogin = false)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                // --- Logout ---
                // Invalider la session et supprimer le cookie JSESSIONID
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) ->
                            response.setStatus(200)
                        )
                );

        return http.build();
    }
}
