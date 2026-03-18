package com.clinic.app.controller;

import com.clinic.app.entity.Patient;
import com.clinic.app.entity.User;
import com.clinic.app.service.AuthService;
import com.clinic.app.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final AuthService authService;

    public PatientController(PatientService patientService, AuthService authService) {
        this.patientService = patientService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Patient patient,
                                    @RequestHeader(value = "X-Token", required = false) String token) {
        if (token == null || !authService.isValid(token)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(patientService.save(patient));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Patient p = patientService.get(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String nom) {
        return ResponseEntity.ok(patientService.search(nom));
    }

    @GetMapping("/export")
    public ResponseEntity<?> export() {
        List<Patient> patients = patientService.getAll();
        StringBuilder csv = new StringBuilder("id,nom,prenom,dateNaissance,numeroSecu,adresse,telephone\n");
        for (Patient p : patients) {
            csv.append(p.getId()).append(",")
               .append(p.getNom()).append(",")
               .append(p.getPrenom()).append(",")
               .append(p.getDateNaissance()).append(",")
               .append(p.getNumeroSecu()).append(",")
               .append(p.getAdresse()).append(",")
               .append(p.getTelephone()).append("\n");
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .body(csv.toString());
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader(value = "X-Token", required = false) String token) {
        User user = authService.getUser(token);
        if (user != null && user.r.equals("ADMIN")) {
            return ResponseEntity.ok(patientService.getAll());
        }
        return ResponseEntity.ok(patientService.getAll());
    }
}
