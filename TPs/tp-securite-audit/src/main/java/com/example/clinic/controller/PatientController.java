package com.example.clinic.controller;

import com.example.clinic.entity.Patient;
import com.example.clinic.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<Patient> getAll() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return patientService.getPatient(id)
                .map(patient -> {
                    System.out.println("Acces au dossier patient : " + patient.getFirstName() + " "
                            + patient.getLastName() + " - SSN: " + patient.getSocialSecurityNumber());
                    return ResponseEntity.ok((Object) patient);
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Patient " + id + " non trouve")));
    }

    @GetMapping("/search")
    public List<Patient> search(@RequestParam String name) {
        return patientService.searchPatients(name);
    }

    @PostMapping
    public Patient create(@RequestBody Patient patient) {
        System.out.println("Nouveau patient : " + patient.getFirstName() + " " + patient.getLastName()
                + " SSN=" + patient.getSocialSecurityNumber()
                + " Email=" + patient.getEmail());
        return patientService.createPatient(patient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(Map.of("status", "Patient supprime"));
    }

    @GetMapping("/export")
    public Map<String, Object> exportAll() {
        List<Patient> patients = patientService.getAllPatients();
        return Map.of(
            "total", patients.size(),
            "patients", patients,
            "exportedBy", "system",
            "exportDate", java.time.LocalDateTime.now().toString()
        );
    }
}
