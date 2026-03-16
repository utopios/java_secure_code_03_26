### TP 1 : **Développement d'un système sécurisé d'authentification et de gestion des mots de passe avec multi-facteurs en Java**

#### Contexte :
Vous développez un système d'authentification sécurisé pour une application sensible. L'objectif est d'implémenter un système de gestion des mots de passe avec des règles de complexité, un suivi des tentatives de connexion, et un système d'authentification multi-facteurs via une **émulation** d'envoi de code OTP.

#### Exigences :

1. **Catégorisation des mots de passe selon le niveau de criticité** :
   - **Important** : minimum 6 caractères, au moins une lettre et un chiffre.
   - **Critique** : minimum 8 caractères avec une minuscule, majuscule, chiffre et caractère spécial.
   - **Hautement critique** : minimum 14 caractères avec les mêmes règles que critique + authentification multi-facteurs obligatoire.

2. **Complexité des mots de passe** :
   - Forcer l'utilisation de lettres, chiffres, et caractères spéciaux.
   - Interdire les séquences simples : numériques (`123`, `456`...), répétitions (`aaa`, `111`...), et clavier (`qwerty`, `azerty`).
   - Empêcher l'utilisation de mots courants issus d'un dictionnaire (`password`, `admin`, `welcome`...).

3. **Hachage sécurisé des mots de passe** :
   - **Utiliser BCrypt** (ou Argon2id) pour le hachage. Ne **jamais** utiliser SHA-256/MD5 seul pour stocker des mots de passe.
   - BCrypt intègre automatiquement un sel unique et un facteur de coût adaptatif.

4. **Gestion des anciens mots de passe et des rappels** :
   - Forcer le changement de mot de passe tous les 30 jours.
   - Ne pas autoriser la réutilisation des 5 derniers mots de passe.
   - Limiter les rappels de mot de passe à une fois toutes les 24 heures.

5. **Gestion des accès dormants** :
   - Marquer un compte comme dormant après 30 jours d'inactivité.
   - Un compte dormant ne peut pas se connecter sans réactivation par un administrateur.

6. **Authentification multi-facteurs (MFA)** :
   - Pour les utilisateurs accédant à des informations hautement critiques, mettre en place un système d'authentification multi-facteurs.
   - **Émuler** l'envoi d'un code OTP qui est affiché dans la console.
   - L'OTP doit expirer après **5 minutes** et être à **usage unique** (supprimé après validation).
   - Utiliser `SecureRandom` (pas `Random`) pour la génération de l'OTP.

7. **Protection contre les attaques par brute force** :
   - Verrouiller un compte après 5 tentatives échouées et le débloquer après 15 minutes.

---

#### Points d'attention sécurité :
- Ne jamais afficher ou loguer un mot de passe en clair (même en mode debug).
- Le stockage du hash doit être opaque : un attaquant ayant accès à la base ne doit pas pouvoir retrouver le mot de passe.
- En production, l'OTP serait envoyé par SMS/email via un service dédié (Twilio, SendGrid, etc.).
