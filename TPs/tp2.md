### TP 2 : **Développement d'un système avancé de contrôle d'accès basé sur les rôles et les permissions avec limitation des transactions et détection des attaques**

#### Contexte :
Vous allez poursuivre le développement du système sécurisé en ajoutant un **contrôle d'accès avancé**. L'objectif est de gérer les **permissions basées sur les rôles**, de limiter les transactions selon le type d'opération, et d'implémenter une détection d'attaques (comme des tentatives de connexion multiples depuis différentes IPs). Ce TP suit le TP 1 et aborde les concepts d'autorisation et de gestion des sessions en intégrant des restrictions supplémentaires.

#### Exigences :

1. **Gestion des rôles et des permissions** :
   - Implémenter un contrôle d'accès basé sur les rôles (RBAC) avec Spring Security.
   - Différencier les utilisateurs en fonction de leur rôle : **Utilisateur**, **Admin**, **Super Admin**.
   - Chaque rôle doit avoir des permissions spécifiques pour accéder à certaines ressources de l'application.
   - Centraliser la gestion des habilitations sur le serveur (aucune habilitation côté client).

2. **Renforcement de la sécurité des sessions** :
   - **Renouveler l'ID de session** après chaque changement de rôle ou de privilège (protection contre le session fixation).
   - **Limiter les sessions simultanées** : un utilisateur ne doit avoir qu'une seule session active à la fois. L'ancienne session doit être invalidée (pas de blocage).
   - **Expiration des sessions** selon le niveau de criticité :
     - Session normale : 1 heure.
     - Session critique (transactionnelle) : 20 minutes.
     - Session hautement critique : 5 minutes.
   - Invalider la session proprement au logout (supprimer le cookie JSESSIONID).

3. **Limitation des transactions par utilisateur** (Bucket4j) :
   - Limiter le nombre de transactions qu'un utilisateur peut effectuer dans un intervalle de temps donné :
     - **Opérations normales** : 10 transactions toutes les 10 minutes.
     - **Opérations critiques (paiements, modifications majeures)** : 5 transactions par heure.
   - Implémenter une solution de **rate-limiting** avec Bucket4j.
   - **Important** : l'identifiant utilisateur doit être récupéré depuis le `SecurityContextHolder`, pas hardcodé.

4. **Protection CSRF** :
   - Activer la protection CSRF avec `CookieCsrfTokenRepository`.
   - Exclure uniquement les endpoints publics (login, register) de la vérification CSRF.

5. **Détection des attaques** :
   - Implémenter un système qui détecte les comportements suspects :
     - **Connexion simultanée depuis différentes IPs** pour un même utilisateur.
     - **Tentatives répétées de transactions** au-delà des limites autorisées.
     - **Tentatives répétées de modification d'informations sensibles** (ex. : mot de passe, email).
   - Loguer ces comportements de manière sécurisée (ne pas loguer de données sensibles).
