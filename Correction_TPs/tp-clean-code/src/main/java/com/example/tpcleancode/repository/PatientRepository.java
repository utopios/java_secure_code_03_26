package com.example.tpcleancode.repository;

import com.example.tpcleancode.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Patient.
 *
 * CORRECTION Securite :
 * - Utilisation de requetes parametrees (@Param) au lieu de concatenation de chaines
 * - Previent l'injection SQL
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Recherche par nom de famille (parametre lie, pas de concatenation).
     * AVANT (vulnerable) : "SELECT * FROM patient WHERE last_name = '" + name + "'"
     * APRES (securise) : requete parametree avec @Param
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Patient> searchByLastName(@Param("lastName") String lastName);

    @Query("SELECT p FROM Patient p WHERE p.doctorName = :doctorName AND p.active = true")
    List<Patient> findByDoctorName(@Param("doctorName") String doctorName);

    List<Patient> findByActiveTrue();
}
