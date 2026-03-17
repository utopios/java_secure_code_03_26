### TP : **Analyse et Remediation des Vulnerabilites avec OWASP Dependency-Check**

#### Contexte :

Vous venez d'integrer une equipe qui developpe une application e-commerce (`vulnerable-app/`). L'application a ete developpee rapidement et les dependances n'ont jamais ete auditees. Votre mission est d'analyser les vulnerabilites presentes dans les dependances, d'evaluer leur criticite, et de les corriger.

Ce TP couvre une competence essentielle du developpeur securise : **la gestion des composants tiers vulnerables** (OWASP Top 10 - A06:2021 Vulnerable and Outdated Components).

---

#### Partie 1 : Installation et configuration de Dependency-Check

1. **Obtenir une cle API NVD** (si pas deja fait) :
   - Aller sur https://nvd.nist.gov/developers/request-an-api-key
   - Remplir le formulaire (email + organisation)
   - Recevoir la cle par email

2. **Ajouter le plugin OWASP Dependency-Check** dans le `pom.xml` de `vulnerable-app/` :
   - Plugin : `org.owasp:dependency-check-maven` version `12.1.0`
   - Configurer :
     - La cle API NVD (via variable d'environnement `NVD_API_KEY`)
     - Le seuil d'echec du build : `failBuildOnCVSS` = **7** (HIGH et CRITICAL)
     - Le format de rapport : **HTML**

3. **Lancer la premiere analyse** :
   ```bash
   export NVD_API_KEY="votre-cle-ici"
   mvn dependency-check:check
   ```

4. **Ouvrir le rapport** : `target/dependency-check-report.html`

**Questions** :
- Combien de vulnerabilites ont ete detectees au total ?
- Combien sont CRITICAL (CVSS >= 9.0) ?
- Combien sont HIGH (CVSS >= 7.0) ?
- Le build a-t-il echoue ? Pourquoi ?

---

#### Partie 2 : Analyse du rapport

Pour chaque vulnerabilite trouvee, remplissez le tableau suivant :

| Dependance | Version actuelle | CVE | CVSS | Severite | Description courte | Impact potentiel |
|-----------|-----------------|-----|------|----------|--------------------|------------------|
| | | | | | | |

**Pour chaque CVE CRITICAL et HIGH, repondez** :
1. Quel est le vecteur d'attaque ? (reseau, local, physique)
2. L'attaquant a-t-il besoin d'etre authentifie ?
3. Quel est l'impact ? (RCE, DoS, fuite de donnees, elevation de privileges)
4. La vulnerabilite est-elle exploitable dans le contexte de NOTRE application ?

**Indices pour l'analyse** :
- Regardez le code source de `ProductService.java` : quelle methode utilise `commons-text` ?
- Regardez `application.properties` : quel composant est expose sur le reseau ?
- Regardez `SecurityConfig.java` : quels endpoints sont ouverts sans authentification ?

---

#### Partie 3 : Remediation des dependances 

Corrigez les vulnerabilites en mettant a jour les dependances dans le `pom.xml`.

**Consignes** :
- Pour chaque dependance vulnerable, rechercher la **version minimale corrigee** (le rapport Dependency-Check l'indique souvent)
- Privilegier la version **stable la plus recente** compatible avec Spring Boot 3.x / Java 17
- Apres chaque correction, relancer `mvn dependency-check:check` pour verifier
