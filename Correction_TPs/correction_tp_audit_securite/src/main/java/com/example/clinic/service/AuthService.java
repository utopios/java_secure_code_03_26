package com.example.clinic.service;

import com.example.clinic.entity.Doctor;
import com.example.clinic.repository.DoctorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class AuthService {

    // CORRIGE #11 : Suppression de l'EntityManager (plus de requetes JPQL manuelles)
    private final DoctorRepository doctorRepository;
    // CORRIGE #12 : BCrypt au lieu de SHA-256 sans sel
    private final PasswordEncoder passwordEncoder;

    // CORRIGE #17 : Protection brute force -- compteur de tentatives par username
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    public AuthService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Doctor authenticate(String username, String password) {
        // CORRIGE #17 : Verifier si le compte est verrouille
        int attempts = loginAttempts.getOrDefault(username, 0);
        if (attempts >= MAX_ATTEMPTS) {
            return null;
        }

        // CORRIGE #11 : Requete parametree via Spring Data JPA
        // AVANT : "SELECT d FROM Doctor d WHERE d.username = '" + username + "' AND ..."
        // L'injection SQL n'est plus possible
        Optional<Doctor> doctorOpt = doctorRepository.findByUsername(username);

        // CORRIGE #12 : Comparaison BCrypt (timing-safe)
        if (doctorOpt.isEmpty() || !passwordEncoder.matches(password, doctorOpt.get().getPassword())) {
            loginAttempts.merge(username, 1, Integer::sum);
            return null;
        }

        // Login reussi : reset du compteur
        loginAttempts.remove(username);
        return doctorOpt.get();
    }

    public boolean isAccountLocked(String username) {
        return loginAttempts.getOrDefault(username, 0) >= MAX_ATTEMPTS;
    }

    public Doctor register(String username, String password, String fullName, String specialty) {
        // CORRIGE #12 : BCrypt
        Doctor doctor = new Doctor(username, passwordEncoder.encode(password), fullName, specialty, "DOCTOR");
        return doctorRepository.save(doctor);
    }

    // SUPPRIME : methode hashPassword() avec SHA-256 sans sel
}
