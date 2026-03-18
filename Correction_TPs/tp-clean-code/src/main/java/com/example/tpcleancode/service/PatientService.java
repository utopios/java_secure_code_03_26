package com.example.tpcleancode.service;

import com.example.tpcleancode.dto.PatientSearchRequest;
import com.example.tpcleancode.entity.Patient;
import com.example.tpcleancode.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des patients.
 *
 * CORRECTIONS Clean Code :
 * - La methode process() avec 4 niveaux de if imbriques est remplacee par
 *   des methodes claires et des early returns (Guard Clauses)
 * - Chaque methode a une seule responsabilite
 * - Noms de methodes explicites
 *
 * CORRECTIONS Securite :
 * - Injection SQL : utilisation de requetes parametrees (voir PatientRepository)
 *   AVANT: "SELECT * FROM patient WHERE last_name = '" + name + "'"
 *   APRES: @Query avec @Param
 * - Le numero de securite sociale n'est JAMAIS logge en clair
 *   AVANT: log.info("Patient SSN: " + patient.getSsn());
 *   APRES: log.info("Patient SSN: {}", patient.getMaskedSsn());
 */
@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Cree un nouveau patient apres validation.
     *
     * CORRECTION Clean Code : remplace la methode process() monolithique
     * avec 4 niveaux de if imbriques par des Guard Clauses.
     *
     * AVANT (vulnerable, illisible) :
     * public Object process(Map<String, Object> data) {
     *     if (data != null) {
     *         if (data.get("name") != null) {
     *             if (data.get("ssn") != null) {
     *                 if (data.get("ssn").toString().length() == 13) {
     *                     // traitement ...
     *                 }
     *             }
     *         }
     *     }
     *     return null;
     * }
     */
    public Patient createPatient(Patient patient) {
        validatePatient(patient);

        Patient saved = patientRepository.save(patient);
        // CORRECTION: SSN masque dans les logs
        logger.info("Patient cree avec succes : id={}, SSN={}",
                saved.getId(), saved.getMaskedSsn());
        return saved;
    }

    /**
     * Recherche des patients par nom de famille.
     * Utilise une requete parametree (pas de concatenation de chaines).
     */
    public List<Patient> searchByLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Le nom de famille est obligatoire pour la recherche");
        }
        logger.info("Recherche de patients par nom de famille");
        return patientRepository.searchByLastName(lastName);
    }

    /**
     * Recupere un patient par son ID.
     */
    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    /**
     * Recupere tous les patients actifs.
     */
    public List<Patient> findAllActive() {
        return patientRepository.findByActiveTrue();
    }

    /**
     * Recupere les patients d'un medecin donne.
     */
    public List<Patient> findByDoctor(String doctorName) {
        if (doctorName == null || doctorName.isBlank()) {
            throw new IllegalArgumentException("Le nom du medecin est obligatoire");
        }
        return patientRepository.findByDoctorName(doctorName);
    }

    // --- Methode de validation privee (Guard Clauses) ---

    private void validatePatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Le patient ne peut pas etre null");
        }
        if (patient.getFirstName() == null || patient.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Le prenom du patient est obligatoire");
        }
        if (patient.getLastName() == null || patient.getLastName().isBlank()) {
            throw new IllegalArgumentException("Le nom du patient est obligatoire");
        }
        if (patient.getSocialSecurityNumber() == null
                || patient.getSocialSecurityNumber().length() != 13) {
            throw new IllegalArgumentException(
                    "Le numero de securite sociale doit contenir exactement 13 caracteres");
        }
    }
}
