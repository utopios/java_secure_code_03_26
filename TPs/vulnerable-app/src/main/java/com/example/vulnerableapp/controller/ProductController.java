package com.example.vulnerableapp.controller;

import com.example.vulnerableapp.service.ProductService;
import com.example.vulnerableapp.dto.ProductRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    public Map<String, Object> createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query);
    }

    /**
     * Endpoint qui utilise Commons Text StringSubstitutor
     * pour formatter des descriptions de produits.
     * VULNERABLE a Text4Shell (CVE-2022-42889) !
     */
    @PostMapping("/format-description")
    public String formatDescription(@RequestBody String template) {
        return productService.formatDescription(template);
    }
}
