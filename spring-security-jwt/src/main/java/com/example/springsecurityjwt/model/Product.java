package com.example.springsecurityjwt.model;



import jakarta.persistence.*;
import jdk.jfr.DataAmount;
import lombok.Data;



@Entity
@Data
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;


}

