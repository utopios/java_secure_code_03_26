package com.example.clinic.service;

import com.example.clinic.entity.Patient;
import com.example.clinic.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    // CORRIGE #22 : Suppression de l'EntityManager
    // Plus besoin de requetes JPQL manuelles (source d'injection SQL)
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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

    // CORRIGE #22 : Requete parametree via Spring Data
    // AVANT : "SELECT p FROM Patient p WHERE p.lastName LIKE '%" + name + "%'"
    // L'injection SQL n'est plus possible
    public List<Patient> searchPatients(String name) {
        return patientRepository.findByLastNameContainingIgnoreCase(name);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}
