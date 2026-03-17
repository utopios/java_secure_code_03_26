package com.example.clinic.service;

import com.example.clinic.entity.Patient;
import com.example.clinic.repository.PatientRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final EntityManager entityManager;

    public PatientService(PatientRepository patientRepository, EntityManager entityManager) {
        this.patientRepository = patientRepository;
        this.entityManager = entityManager;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatient(Long id) {
        return patientRepository.findById(id);
    }

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Patient> searchPatients(String name) {
        String jpql = "SELECT p FROM Patient p WHERE p.lastName LIKE '%" + name + "%'";
        return entityManager.createQuery(jpql, Patient.class).getResultList();
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}
