package com.example.clinic.controller;

import com.example.clinic.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// CORRIGE #24 : L'acces est restreint par SecurityConfig :
//   .requestMatchers("/api/admin/**").hasRole("ADMIN")
// Seul un admin authentifie peut acceder a ces endpoints.
// AVANT : aucun controle -- n'importe qui pouvait supprimer des docteurs, changer des roles, etc.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final DoctorRepository doctorRepository;

    public AdminController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/doctors")
    public List<Map<String, Object>> getAllDoctors() {
        // CORRIGE : Ne pas exposer le hash du mot de passe dans la liste
        return doctorRepository.findAll().stream()
                .map(d -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", d.getId());
                    map.put("username", d.getUsername());
                    map.put("fullName", d.getFullName());
                    map.put("specialty", d.getSpecialty());
                    map.put("role", d.getRole());
                    // PAS de d.getPassword() -- le hash ne doit jamais sortir de la base
                    return map;
                })
                .collect(Collectors.toList());
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        log.info("Suppression du docteur id={}", id);
        doctorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "Docteur supprime"));
    }

    @PutMapping("/doctors/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");

        // CORRIGE : Validation du role par whitelist
        // AVANT : n'importe quelle valeur etait acceptee (doctor.setRole(body.get("role")))
        if (!"ADMIN".equals(newRole) && !"DOCTOR".equals(newRole)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Role invalide. Valeurs autorisees : ADMIN, DOCTOR"
            ));
        }

        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctor.setRole(newRole);
                    doctorRepository.save(doctor);
                    log.info("Role du docteur id={} modifie en {}", id, newRole);
                    return ResponseEntity.ok(Map.of("status", "Role modifie", "newRole", doctor.getRole()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // CORRIGE #25 : Endpoint /system-info SUPPRIME
    //
    // AVANT :
    //   @GetMapping("/system-info")
    //   public Map<String, Object> systemInfo() {
    //       return Map.of(
    //           "javaVersion", System.getProperty("java.version"),
    //           "osName", System.getProperty("os.name"),
    //           "osVersion", System.getProperty("os.version"),
    //           "userDir", System.getProperty("user.dir"),
    //           "userName", System.getProperty("user.name"),
    //           "freeMemory", Runtime.getRuntime().freeMemory(),
    //           "totalMemory", Runtime.getRuntime().totalMemory()
    //       );
    //   }
    //
    // POURQUOI : exposer la version Java, l'OS, le username systeme et les chemins
    // permet a un attaquant de preparer une attaque ciblee (fingerprinting).

    // CORRIGE #26 : Endpoint /run SUPPRIME
    //
    // AVANT :
    //   @GetMapping("/run")
    //   public Map<String, Object> runCommand(@RequestParam String cmd) {
    //       Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
    //       String output = new String(process.getInputStream().readAllBytes());
    //       return Map.of("output", output);
    //   }
    //
    // POURQUOI : execution de commande OS arbitraire = RCE (Remote Code Execution).
    // Un attaquant peut lire des fichiers, installer des backdoors, pivoter dans le reseau.
    // C'est la faille la plus CRITIQUE de l'application.
    // Il n'existe AUCUN cas d'usage legitime pour un tel endpoint en production.
}
