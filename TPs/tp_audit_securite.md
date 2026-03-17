### TP : **Audit de Securite d'une Application Medicale**

#### Contexte

Vous etes missionne pour auditer **ClinicApp**, une application de gestion de patients et d'ordonnances developpee en Spring Boot. L'application a ete mise en production rapidement et aucune revue de securite n'a ete effectuee.

Le projet se trouve dans le dossier `tp-securite-audit/`.

Votre mission :
1. Identifier toutes les failles de securite presentes dans le code
2. Classifier chaque faille selon le OWASP Top 10
3. Proposer et implementer les corrections

**Donnees de demo** :
- Docteurs : `dr.martin` / `martin2024`, `dr.dupont` / `dupont2024`, `admin` / `admin`
- Patients : Jean Lemaire (id=1), Marie Durand (id=2), Luc Bernard (id=3)
- Prescriptions : 2 ordonnances (ids 1 et 2)

Lancez l'application : `mvn spring-boot:run`

---

#### Partie 1 : Identification des failles 

Parcourez le code source et testez les endpoints avec curl ou Postman. Pour chaque faille trouvee, remplissez une ligne dans le tableau ci-dessous.

| # | Fichier | Ligne(s) | Categorie OWASP | Description | Severite |
|---|---------|----------|-----------------|-------------|----------|
| 1 | | | | | |
| 2 | | | | | |
| ... | | | | | |

**Fichiers a auditer** :
- `config/SecurityConfig.java`
- `config/DataInitializer.java`
- `service/AuthService.java`
- `service/PatientService.java`
- `controller/AuthController.java`
- `controller/PatientController.java`
- `controller/PrescriptionController.java`
- `controller/AdminController.java`
- `application.properties`

**Indices** : il y a au moins **20 failles** reparties dans ces fichiers. Cherchez dans ces categories :
- Injection (SQL, OS)
- Authentification et sessions
- Exposition de donnees sensibles
- Controle d'acces manquant
- Mauvaise configuration
- Logging dangereux

---

#### Partie 2 : Correction des failles

Corrigez toutes les failles identifiees. 


## Correction

SecurityConfig.java
#	Ligne	Faille	OWASP	Severite
1	16	csrf.disable() sans justification	A05 - Misconfiguration	ELEVE
2	18-20	Tout en permitAll() -- aucune authentification requise	A07 - Controle d'acces	CRITIQUE
3	19	Console H2 accessible sans auth	A05 - Misconfiguration	CRITIQUE
4	22	frameOptions.disable() -- clickjacking possible	A05 - Misconfiguration	MOYEN
5	--	Pas de headers de securite (CSP, HSTS, nosniff)	A05 - Misconfiguration	MOYEN
6	--	Pas de PasswordEncoder bean	A02 - Authentification	ELEVE
application.properties
#	Ligne	Faille	OWASP	Severite
7	8	spring.h2.console.enabled=true en production	A05 - Misconfiguration	CRITIQUE
8	14-15	include-stacktrace=always + include-message=always	A05 - Misconfiguration	MOYEN
DataInitializer.java
#	Ligne	Faille	OWASP	Severite
9	65-68	SHA-256 sans sel pour les mots de passe (au lieu de BCrypt)	A02 - Authentification	CRITIQUE
10	34	Mot de passe admin = "admin" (identifiant par defaut)	A07 - Controle d'acces	CRITIQUE
AuthService.java
#	Ligne	Faille	OWASP	Severite
11	25-26	Injection SQL : concatenation du username dans la requete JPQL	A01 - Injection	CRITIQUE
12	39-42	SHA-256 sans sel (meme probleme que DataInitializer)	A02 - Authentification	CRITIQUE
AuthController.java
#	Ligne	Faille	OWASP	Severite
13	29	Message d'erreur qui revele le username ("Utilisateur dr.martin non trouve") -- enumeration	A02 - Authentification	MOYEN
14	33	System.out.println du mot de passe en clair dans les logs	A06 - Donnees sensibles	CRITIQUE
15	40	Hash du mot de passe retourne dans la reponse API ("password": "$hash")	A06 - Donnees sensibles	CRITIQUE
16	41	Token de session predictible : "session-" + doctor.getId() (1, 2, 3...)	A02 - Authentification	ELEVE
17	--	Pas de protection brute force (tentatives illimitees)	A02 - Authentification	ELEVE
PatientController.java
#	Ligne	Faille	OWASP	Severite
18	30-31	Log du SSN (numero de securite sociale) en clair	A06 - Donnees sensibles	CRITIQUE
19	44-46	Log du SSN + email a la creation	A06 - Donnees sensibles	CRITIQUE
20	21-24	Endpoint /api/patients retourne le SSN, les notes medicales en clair a tout le monde	A06 - Donnees sensibles	CRITIQUE
21	56-64	/api/patients/export : export de masse sans auth (SSN, donnees medicales)	A04 - IDOR / A06	CRITIQUE
PatientService.java
#	Ligne	Faille	OWASP	Severite
22	35	Injection SQL : concatenation du parametre name dans la requete JPQL	A01 - Injection	CRITIQUE
PrescriptionController.java
#	Ligne	Faille	OWASP	Severite
23	21-47	Aucun controle d'acces : n'importe qui peut creer/lire/supprimer des ordonnances	A07 - Controle d'acces	CRITIQUE
AdminController.java
#	Ligne	Faille	OWASP	Severite
24	21-41	Endpoints admin sans verification de role ADMIN	A07 - Controle d'acces	CRITIQUE
25	43-53	/system-info expose version Java, OS, username, chemins serveur	A05 - Misconfiguration	ELEVE
26	56-65	/run?cmd=... : execution de commande OS arbitraire sans aucune restriction	A01 - Injection (OS Command)	CRITIQUE