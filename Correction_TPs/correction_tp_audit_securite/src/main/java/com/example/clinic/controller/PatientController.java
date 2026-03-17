package com.example.clinic.controller;

import com.example.clinic.entity.Patient;
import com.example.clinic.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        // CORRIGE #20 : Vue filtree -- pas de SSN ni de notes medicales
        // AVANT : retournait l'entite Patient complete (SSN, medicalNotes en clair)
        return patientService.getAllPatients().stream()
                .map(this::toSafeView)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return patientService.getPatient(id)
                .map(patient -> {
                    // CORRIGE #18 : Log SANS SSN ni donnees medicales
                    // AVANT : System.out.println("Acces au dossier patient : " + firstName
                    //         + " " + lastName + " - SSN: " + socialSecurityNumber);
                    log.info("Acces au dossier patient id={}", patient.getId());
                    return ResponseEntity.ok((Object) toSafeView(patient));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Patient non trouve")));
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String name) {
        // La recherche utilise maintenant PatientRepository.findByLastNameContainingIgnoreCase()
        // (requete parametree, pas de concatenation JPQL)
        return patientService.searchPatients(name).stream()
                .map(this::toSafeView)
                .collect(Collectors.toList());
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Patient patient) {
        // CORRIGE #19 : Log SANS donnees sensibles
        // AVANT : System.out.println("Nouveau patient : " + firstName + " " + lastName
        //         + " SSN=" + socialSecurityNumber + " Email=" + email);
        log.info("Creation d'un nouveau patient");
        Patient saved = patientService.createPatient(patient);
        return toSafeView(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("Suppression du patient id={}", id);
        patientService.deletePatient(id);
        return ResponseEntity.ok(Map.of("status", "Patient supprime"));
    }

    // CORRIGE #21 : Export avec SSN masque et sans notes medicales
    // AVANT : retournait toutes les donnees brutes y compris SSN en clair
    @GetMapping("/export")
    public Map<String, Object> exportAll() {
        List<Map<String, Object>> patients = patientService.getAllPatients().stream()
                .map(this::toExportView)
                .collect(Collectors.toList());
        return Map.of(
            "total", patients.size(),
            "patients", patients,
            "exportDate", java.time.LocalDateTime.now().toString()
        );
    }

    // CORRIGE #20 : Vue filtree pour les endpoints courants
    // Le SSN et les notes medicales ne sont JAMAIS exposes dans les reponses standard
    private Map<String, Object> toSafeView(Patient p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("firstName", p.getFirstName());
        map.put("lastName", p.getLastName());
        map.put("email", maskEmail(p.getEmail()));
        map.put("phone", p.getPhone());
        // SSN : EXCLU
        // medicalNotes : EXCLU
        return map;
    }

    // CORRIGE #21 : Vue export avec SSN masque (pour les exports administratifs)
    private Map<String, Object> toExportView(Patient p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("firstName", p.getFirstName());
        map.put("lastName", p.getLastName());
        map.put("email", p.getEmail());
        map.put("ssn", maskSSN(p.getSocialSecurityNumber()));
        // medicalNotes : EXCLU de l'export
        return map;
    }

    private String maskEmail(String email) {
        if (email == null) return "***";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "****" + email.substring(at);
    }

    private String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 5) return "***";
        return "*** *** " + ssn.substring(ssn.length() - 5);
    }
}
