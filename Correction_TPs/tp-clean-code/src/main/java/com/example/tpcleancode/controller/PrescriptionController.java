package com.example.tpcleancode.controller;

import com.example.tpcleancode.dto.PrescriptionRequest;
import com.example.tpcleancode.entity.Patient;
import com.example.tpcleancode.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controleur de gestion des prescriptions.
 *
 * CORRECTION Clean Code :
 * - Utilise un DTO type (PrescriptionRequest) au lieu de Map<String, Object>
 *   AVANT: @RequestBody Map<String, Object> params
 *          String med = (String) params.get("medication");
 *          int days = (int) params.get("days");
 *   APRES: @Valid @RequestBody PrescriptionRequest request
 *          request.getMedication(), request.getDurationDays()
 *
 * - Validation automatique via Jakarta Bean Validation (@Valid)
 * - Plus de casts manuels ni de NullPointerException possibles
 */
@RestController
@RequestMapping("/api/prescriptions")
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
public class PrescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    private final PatientService patientService;

    public PrescriptionController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Cree une nouvelle prescription pour un patient.
     *
     * CORRECTION : DTO type avec validation au lieu de Map<String, Object>
     */
    @PostMapping
    public ResponseEntity<?> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        // Verifier que le patient existe
        Optional<Patient> patient = patientService.findById(request.getPatientId());
        if (patient.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Patient non trouve avec l'ID: " + request.getPatientId()));
        }

        logger.info("Prescription creee : patient={}, medicament={}, duree={} jours",
                request.getPatientId(),
                request.getMedication(),
                request.getDurationDays());

        // Retourner la confirmation
        return ResponseEntity.ok(Map.of(
                "message", "Prescription creee avec succes",
                "patientId", request.getPatientId(),
                "medication", request.getMedication(),
                "dosage", request.getDosage(),
                "frequency", request.getFrequency(),
                "durationDays", request.getDurationDays()
        ));
    }
}
