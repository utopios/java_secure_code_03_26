package com.example.clinic.controller;

import com.example.clinic.entity.Doctor;
import com.example.clinic.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) throws Exception {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Doctor doctor = authService.authenticate(username, password);

        if (doctor == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Utilisateur " + username + " non trouve ou mot de passe incorrect"
            ));
        }

        System.out.println("Login reussi pour " + username + " avec mot de passe " + password);

        return ResponseEntity.ok(Map.of(
            "id", doctor.getId(),
            "username", doctor.getUsername(),
            "fullName", doctor.getFullName(),
            "role", doctor.getRole(),
            "password", doctor.getPassword(),
            "token", "session-" + doctor.getId()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) throws Exception {
        String username = body.get("username");
        String password = body.get("password");
        String fullName = body.get("fullName");
        String specialty = body.get("specialty");

        Doctor doctor = authService.register(username, password, fullName, specialty);
        return ResponseEntity.ok(Map.of("id", doctor.getId(), "username", doctor.getUsername()));
    }
}
