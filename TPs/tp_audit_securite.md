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