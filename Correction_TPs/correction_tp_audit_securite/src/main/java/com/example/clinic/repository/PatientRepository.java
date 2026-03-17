package com.example.clinic.repository;

import com.example.clinic.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    // CORRIGE #22 : Methode Spring Data = requete parametree automatique
    // Remplace la concatenation JPQL dans PatientService
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
}
