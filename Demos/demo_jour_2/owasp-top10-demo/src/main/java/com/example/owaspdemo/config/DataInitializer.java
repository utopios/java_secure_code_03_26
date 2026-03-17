package com.example.owaspdemo.config;

import com.example.owaspdemo.entity.Product;
import com.example.owaspdemo.entity.User;
import com.example.owaspdemo.repository.ProductRepository;
import com.example.owaspdemo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Utilisateurs de demo
        userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "admin@company.com", "ADMIN"));
        userRepository.save(new User("alice", passwordEncoder.encode("password1"), "alice@company.com", "USER"));
        userRepository.save(new User("bob", passwordEncoder.encode("password2"), "bob@company.com", "USER"));

        // Produits de demo
        productRepository.save(new Product("Laptop Pro", "Ordinateur portable 16 pouces", 1499.99, "alice"));
        productRepository.save(new Product("Casque Audio", "Casque Bluetooth", 249.99, "alice"));
        productRepository.save(new Product("Clavier Mecanique", "Switches Cherry MX", 149.99, "bob"));
        productRepository.save(new Product("Ecran 4K", "Moniteur 27 pouces", 599.99, "bob"));
        productRepository.save(new Product("Document Confidentiel", "Rapport financier Q1", 0.0, "admin"));
    }
}
