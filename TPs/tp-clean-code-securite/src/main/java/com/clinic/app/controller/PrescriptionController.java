package com.clinic.app.controller;

import com.clinic.app.entity.Prescription;
import com.clinic.app.service.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long patientId = Long.valueOf(body.get("patientId").toString());
        Long medecinId = Long.valueOf(body.get("medecinId").toString());
        String medicament = body.get("medicament").toString();
        String dosage = body.get("dosage").toString();
        return ResponseEntity.ok(prescriptionService.create(patientId, medecinId, medicament, dosage));
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<Prescription>> getByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getForPatient(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestParam String statut) {
        return ResponseEntity.ok(prescriptionService.update(id, statut));
    }
}
