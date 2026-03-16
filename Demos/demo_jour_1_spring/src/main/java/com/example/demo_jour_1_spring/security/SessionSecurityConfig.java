package com.example.demo_jour_1_spring.security;


import jakarta.servlet.SessionCookieConfig;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SessionSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
        httpSecurity.sessionManagement(session -> {
            session.sessionFixation().changeSessionId()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1);
        });

        return httpSecurity.build();
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            SessionCookieConfig cookieConfig = servletContext.getSessionCookieConfig();

            cookieConfig.setName("__SESSION_ID");   // Masquer la techno
            cookieConfig.setHttpOnly(true);          // Anti-XSS
            cookieConfig.setSecure(true);            // HTTPS uniquement
            cookieConfig.setMaxAge(1800);            // 30 minutes
            cookieConfig.setPath("/");               // Portée restreinte
            // cookieConfig.setDomain(".monsite.com"); // Si multi-sous-domaine
        };
    }

    /*@Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("__SESSION_ID");
        serializer.setSameSite("Lax");              // Anti-CSRF
        serializer.setUseSecureCookie(true);
        serializer.setUseHttpOnlyCookie(true);
        serializer.setCookieMaxAge(1800);
        serializer.setCookiePath("/");
        return serializer;
    }*/
}
