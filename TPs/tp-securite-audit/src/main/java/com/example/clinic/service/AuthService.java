package com.example.clinic.service;

import com.example.clinic.entity.Doctor;
import com.example.clinic.repository.DoctorRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@Service
public class AuthService {

    private final EntityManager entityManager;
    private final DoctorRepository doctorRepository;

    public AuthService(EntityManager entityManager, DoctorRepository doctorRepository) {
        this.entityManager = entityManager;
        this.doctorRepository = doctorRepository;
    }

    public Doctor authenticate(String username, String password) throws Exception {
        String hashedPassword = hashPassword(password);
        String jpql = "SELECT d FROM Doctor d WHERE d.username = '" + username
                    + "' AND d.password = '" + hashedPassword + "'";
        List<Doctor> results = entityManager.createQuery(jpql, Doctor.class).getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public Doctor register(String username, String password, String fullName, String specialty) throws Exception {
        Doctor doctor = new Doctor(username, hashPassword(password), fullName, specialty, "DOCTOR");
        return doctorRepository.save(doctor);
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
