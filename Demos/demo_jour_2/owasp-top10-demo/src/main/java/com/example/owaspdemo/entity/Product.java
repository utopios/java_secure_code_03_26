package com.example.owaspdemo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private String owner; // username du proprietaire

    public Product() {}

    public Product(String name, String description, double price, String owner) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}
