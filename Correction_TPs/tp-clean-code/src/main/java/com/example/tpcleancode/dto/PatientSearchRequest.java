package com.example.tpcleancode.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO pour la recherche de patients.
 *
 * CORRECTION Securite :
 * - Validation des parametres de recherche pour eviter l'injection SQL
 * - Taille limitee pour les champs de recherche
 */
public class PatientSearchRequest {

    @Size(max = 100, message = "Le nom ne doit pas depasser 100 caracteres")
    private String lastName;

    @Size(max = 100, message = "Le prenom ne doit pas depasser 100 caracteres")
    private String firstName;

    @Size(max = 100, message = "Le nom du medecin ne doit pas depasser 100 caracteres")
    private String doctorName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
}
