package com.clinic.app.controller;

import com.clinic.app.dto.LoginRequest;
import com.clinic.app.service.AuthService;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest req) throws Exception {
        Map<String, Object> result = authService.doLogin(req.u, req.p);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(@RequestParam String token) {
        boolean valid = authService.isValid(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
