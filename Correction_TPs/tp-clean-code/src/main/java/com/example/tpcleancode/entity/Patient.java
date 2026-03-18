package com.example.tpcleancode.entity;

import jakarta.persistence.*;

/**
 * Entite Patient.
 *
 * CORRECTION Securite :
 * - Le numero de securite sociale (socialSecurityNumber) est stocke
 *   mais JAMAIS logge en clair (voir PatientService).
 */
@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String socialSecurityNumber;

    @Column
    private String diagnosis;

    @Column
    private String doctorName;

    @Column(nullable = false)
    private boolean active = true;

    // --- Constructeurs ---

    public Patient() {
    }

    public Patient(String firstName, String lastName, String socialSecurityNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.socialSecurityNumber = socialSecurityNumber;
    }

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Retourne le SSN masque pour le logging (ex: "***-**-1234").
     * CORRECTION : ne jamais loguer le SSN complet.
     */
    public String getMaskedSsn() {
        if (socialSecurityNumber == null || socialSecurityNumber.length() < 4) {
            return "***";
        }
        return "***-**-" + socialSecurityNumber.substring(socialSecurityNumber.length() - 4);
    }
}
