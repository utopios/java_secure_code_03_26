
## Exercice 1 : Identifier les erreurs de chiffrement

**Objectif** : Reperer les failles de securite dans du code existant.

Le code suivant contient **5 erreurs de securite**. Identifiez-les et expliquez pourquoi chacune est dangereuse.

```java
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Random;

public class InsecureCrypto {

    // public byte[] encrypt(String data) throws Exception {
        
    //     //ERREUR 1 Taille de clé 128 il faut 256
    //     KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    //     keyGen.init(128);
    //     SecretKey key = keyGen.generateKey();

    //     //Erreur 2 AES, ECB
    //     Cipher cipher = Cipher.getInstance("AES");
    //     cipher.init(Cipher.ENCRYPT_MODE, key);

    //     byte[] iv = new byte[12];
    //     //Erreur 3, Utiliser le SecureRandom au lieu du Random
    //     new Random().nextBytes(iv);


    //     //Erreur 4 IV Absent
    //     byte[] encrypted = cipher.doFinal(data.getBytes());
        
    //     //Erreur 5 La clé est dans les logs
    //     System.out.println("Cle utilisee : " + java.util.Base64.getEncoder().encodeToString(key.getEncoded()));

    //     return encrypted;
    // }

    private static final int AES_KEY_SIZE = 256;         // Correction erreur 1
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public byte[] encrypt(String data, SecretKey key) throws Exception {
        // Correction erreur 3 : SecureRandom au lieu de Random
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // Correction erreur 2 : AES/GCM/NoPadding au lieu de "AES"
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encrypted = cipher.doFinal(data.getBytes());

        // Correction erreur 4 : PAS de log de la cle
        // (on peut loguer l'operation, mais JAMAIS la cle)

        // Correction erreur 5 : concatener IV + ciphertext
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return result;
    }
}
```

**Questions** :
1. Quelle taille de cle AES faut-il utiliser au minimum en production ?
2. Que se passe-t-il quand on ecrit `Cipher.getInstance("AES")` sans mode ?
3. Pourquoi `Random` est-il dangereux ici ?
4. Quel est le probleme avec le `System.out.println` ?
5. Que manque-t-il dans la valeur retournee pour que le destinataire puisse dechiffrer ?

---

## Exercice 2 : Chiffrement AES-GCM basique

**Objectif** : Implementer un chiffrement/dechiffrement AES-GCM correct.

Creez une classe `SimpleAESGCM` avec deux methodes :

```java
public class SimpleAESGCM {

    /**
     * Chiffre un message avec AES-GCM.
     * La methode doit :
     * - Generer un IV de 12 octets avec SecureRandom
     * - Chiffrer avec AES/GCM/NoPadding et un tag de 128 bits
     * - Retourner IV + ciphertext concatenes (IV en premier)
     */
    public byte[] encrypt(String plaintext, SecretKey key) throws Exception {
        // A implementer
    }

    /**
     * Dechiffre un message chiffre avec la methode encrypt().
     * La methode doit :
     * - Extraire l'IV (12 premiers octets)
     * - Extraire le ciphertext (le reste)
     * - Dechiffrer et retourner le texte en clair
     */
    public String decrypt(byte[] ciphertextWithIv, SecretKey key) throws Exception {
        // A implementer
    }
}
```

**Test a realiser** :

```java
public static void main(String[] args) throws Exception {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(256);
    SecretKey key = keyGen.generateKey();

    SimpleAESGCM crypto = new SimpleAESGCM();
    String message = "Donnees confidentielles";

    // Chiffrer
    byte[] encrypted = crypto.encrypt(message, key);

    // Dechiffrer
    String decrypted = crypto.decrypt(encrypted, key);

    // Verifier
    System.out.println("Original  : " + message);
    System.out.println("Dechiffre : " + decrypted);
    System.out.println("Identique : " + message.equals(decrypted));

    // Verifier que deux chiffrements donnent des resultats differents
    byte[] encrypted2 = crypto.encrypt(message, key);
    System.out.println("Chiffres differents : " + !java.util.Arrays.equals(encrypted, encrypted2));
}
```

**Resultat attendu** :
```
Original  : Donnees confidentielles
Dechiffre : Donnees confidentielles
Identique : true
Chiffres differents : true
```

---

## Exercice 3 : Detecter l'alteration avec GCM

**Objectif** : Comprendre la difference entre CBC (sans integrite) et GCM (avec integrite).

Completez la classe suivante pour demontrer que GCM detecte toute alteration :

```java
public class IntegrityDemo {

    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();

        String message = "Virement de 100 EUR";

        // 1. Chiffrer le message avec AES-GCM
        // (reutilisez votre classe SimpleAESGCM de l'exercice 2)

        // 2. Alterer 1 octet du ciphertext (apres l'IV, donc a partir de l'index 12)
        //    encrypted[15] ^= 0x01;

        // 3. Tenter de dechiffrer le message altere
        //    --> Quelle exception est levee ?
        //    --> Que signifie cette exception ?

        // 4. Maintenant, modifier le MESSAGE chiffre pour changer "100" en "999"
        //    --> Est-ce possible avec GCM ? Pourquoi ?
    }
}
```

**Questions** :
1. Quelle exception Java est levee quand GCM detecte une alteration ?
2. Avec AES/CBC simple (sans HMAC), cette alteration serait-elle detectee ?
3. Dans quel scenario reel un attaquant tenterait-il de modifier un ciphertext ?

---

## Exercice 4 : Utiliser les AAD (Associated Authenticated Data)

**Objectif** : Comprendre et utiliser les donnees associees authentifiees de GCM.

Scenario : Vous chiffrez des virements bancaires. Le montant est chiffre, mais l'identifiant
de l'expediteur doit rester en clair (pour le routage) tout en etant protege contre la modification.

```java
public class AADDemo {

    /**
     * Chiffre un montant avec AES-GCM en associant un identifiant utilisateur comme AAD.
     * L'AAD n'est PAS chiffre, mais son integrite est verifiee au dechiffrement.
     *
     * @param amount     le montant a chiffrer (ex: "1500.00")
     * @param userId     l'identifiant utilisateur (AAD, en clair)
     * @param key        la cle AES
     * @return IV + ciphertext + tag
     */
    public byte[] encryptWithAAD(String amount, String userId, SecretKey key) throws Exception {
        // A implementer :
        // 1. Generer un IV
        // 2. Initialiser le Cipher en mode GCM
        // 3. Appeler cipher.updateAAD(userId.getBytes()) AVANT doFinal
        // 4. Chiffrer le montant
        // 5. Retourner IV + ciphertext
    }

    /**
     * Dechiffre le montant et verifie que l'AAD correspond.
     * Si l'userId a ete modifie, le dechiffrement echoue.
     */
    public String decryptWithAAD(byte[] ciphertextWithIv, String userId, SecretKey key) throws Exception {
        // A implementer
    }
}
```

**Tests a realiser** :

```java
public static void main(String[] args) throws Exception {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(256);
    SecretKey key = keyGen.generateKey();

    AADDemo demo = new AADDemo();

    // Test 1 : chiffrement et dechiffrement normal
    byte[] encrypted = demo.encryptWithAAD("1500.00", "USER-42", key);
    String decrypted = demo.decryptWithAAD(encrypted, "USER-42", key);
    System.out.println("Montant dechiffre : " + decrypted);  // 1500.00

    // Test 2 : un attaquant tente de changer l'userId
    try {
        demo.decryptWithAAD(encrypted, "USER-99", key);  // userId modifie !
        System.out.println("ERREUR : ca n'aurait pas du marcher !");
    } catch (javax.crypto.AEADBadTagException e) {
        System.out.println("Attaque detectee : l'userId a ete modifie !");
    }
}
```

**Question** : Dans quel cas metier les AAD sont-elles utiles ? Donnez 3 exemples concrets.