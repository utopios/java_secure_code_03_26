package com.example.tpcleancode.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la creation d'une prescription.
 *
 * CORRECTION Clean Code :
 * - DTO type au lieu de Map<String, Object> (PrescriptionController)
 * - Chaque champ est valide avec des annotations Jakarta
 * - Noms de champs explicites
 */
public class PrescriptionRequest {

    @NotNull(message = "L'ID du patient est obligatoire")
    private Long patientId;

    @NotBlank(message = "Le nom du medicament est obligatoire")
    @Size(max = 200, message = "Le nom du medicament ne doit pas depasser 200 caracteres")
    private String medication;

    @NotBlank(message = "Le dosage est obligatoire")
    @Pattern(regexp = "^\\d+(\\.\\d+)?\\s?(mg|g|ml|UI)$",
            message = "Format de dosage invalide (ex: 500 mg, 1.5 g)")
    private String dosage;

    @NotBlank(message = "La frequence est obligatoire")
    private String frequency;

    @Min(value = 1, message = "La duree doit etre d'au moins 1 jour")
    @Max(value = 365, message = "La duree ne doit pas depasser 365 jours")
    private int durationDays;

    @Size(max = 500, message = "Les notes ne doivent pas depasser 500 caracteres")
    private String notes;

    // --- Getters / Setters ---

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
