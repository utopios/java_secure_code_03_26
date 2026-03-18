package com.clinic.app.config;

import com.clinic.app.entity.User;
import com.clinic.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        User admin = new User();
        admin.n = "admin";
        admin.p = hash("admin123");
        admin.e = "admin@clinic.fr";
        admin.r = "ADMIN";
        admin.a = true;

        User medecin = new User();
        medecin.n = "dr.martin";
        medecin.p = hash("password");
        medecin.e = "martin@clinic.fr";
        medecin.r = "MEDECIN";
        medecin.a = true;

        userRepository.save(admin);
        userRepository.save(medecin);
    }

    public String hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] result = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
