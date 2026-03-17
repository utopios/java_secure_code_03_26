package com.example.clinic.controller;

import com.example.clinic.entity.Prescription;
import com.example.clinic.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// CORRIGE #23 : L'acces est maintenant restreint par SecurityConfig :
//   .requestMatchers("/api/prescriptions/**").hasAnyRole("ADMIN", "DOCTOR")
// Seuls les docteurs et admins authentifies peuvent acceder a ces endpoints.
// AVANT : aucun controle -- n'importe qui pouvait creer/lire/supprimer des ordonnances
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionController.class);
    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    public List<Prescription> getAll() {
        return prescriptionService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return prescriptionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Prescription create(@RequestBody Prescription prescription) {
        log.info("Nouvelle prescription creee pour patient id={}", prescription.getPatientId());
        return prescriptionService.create(prescription);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("Suppression prescription id={}", id);
        prescriptionService.delete(id);
        return ResponseEntity.ok(Map.of("status", "Prescription supprimee"));
    }

    @GetMapping("/patient/{patientId}")
    public List<Prescription> getByPatient(@PathVariable Long patientId) {
        return prescriptionService.getByPatient(patientId);
    }
}
