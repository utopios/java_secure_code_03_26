package com.example.vulnerableapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public class ProductRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String name;

    private String description;

    @DecimalMin(value = "0.01", message = "Le prix doit etre superieur a 0")
    private double price;

    private String category;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
