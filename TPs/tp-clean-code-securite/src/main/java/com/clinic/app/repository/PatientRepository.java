package com.clinic.app.repository;

import com.clinic.app.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query(value = "SELECT * FROM patients WHERE nom LIKE '%?1%'", nativeQuery = true)
    List<Patient> searchByNom(String nom);
}
