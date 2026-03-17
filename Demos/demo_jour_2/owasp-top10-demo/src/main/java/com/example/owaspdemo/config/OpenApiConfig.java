package com.example.owaspdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI owaspOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OWASP Top 10 2021 - Demo Interactive")
                        .description("""
                                Demo interactive des 10 vulnerabilites web les plus critiques (OWASP Top 10 2021).

                                Chaque categorie dispose d'endpoints **VULNERABLE** et **SECURISE** pour comparer les approches.

                                **Comment utiliser cette demo :**
                                1. Testez d'abord l'endpoint VULNERABLE pour observer la faille
                                2. Testez ensuite l'endpoint SECURISE pour voir la correction
                                3. Comparez les reponses

                                **Donnees de demo disponibles :**
                                - Users : admin (ADMIN), alice (USER), bob (USER)
                                - Mots de passe : admin123, password1, password2
                                - Produits : IDs 1 a 5 (le 5 est un document confidentiel de l'admin)
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Formation Java Secure Coding")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Serveur local")))
                .tags(List.of(
                        new Tag().name("A01 - Injection")
                                .description("SQL Injection, OS Command Injection. Attaque : des donnees non fiables sont envoyees a un interpreteur."),
                        new Tag().name("A02 - Authentification")
                                .description("Brute force, enumeration d'utilisateurs, tokens predictibles. Attaque : mecanismes d'authentification defaillants."),
                        new Tag().name("A03 - XSS")
                                .description("Cross-Site Scripting (Reflected, Stored). Attaque : injection de scripts dans les pages web."),
                        new Tag().name("A04 - IDOR")
                                .description("Insecure Direct Object References. Attaque : acces a des objets sans verification de propriete."),
                        new Tag().name("A05 - Misconfiguration")
                                .description("Mauvaise configuration de securite. Attaque : stack traces, headers manquants, console H2 exposee."),
                        new Tag().name("A06 - Donnees sensibles")
                                .description("Exposition de donnees sensibles. Attaque : mots de passe exposes, chiffrement faible, logs dangereux."),
                        new Tag().name("A07 - Controle d'acces")
                                .description("Manque de controle d'acces fonctionnel. Attaque : acces aux fonctions admin sans verification de role."),
                        new Tag().name("A08 - CSRF/SSRF")
                                .description("Cross-Site Request Forgery et Server-Side Request Forgery. Attaque : requetes forgees."),
                        new Tag().name("A09 - Composants vulnerables")
                                .description("Utilisation de composants avec des CVE connues. Log4Shell, Text4Shell, Spring4Shell."),
                        new Tag().name("A10 - Redirections")
                                .description("Redirections et transferts non valides. Attaque : phishing via open redirect.")
                ));
    }
}
