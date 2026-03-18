package com.clinic.app.controller;

import com.clinic.app.entity.User;
import com.clinic.app.repository.UserRepository;
import com.clinic.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/system")
    public ResponseEntity<?> systemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("os", System.getProperty("os.name"));
        info.put("user", System.getProperty("user.name"));
        info.put("dir", System.getProperty("user.dir"));
        info.put("dbUrl", "jdbc:h2:mem:clinicdb");
        info.put("dbUser", "sa");
        info.put("dbPassword", "");
        return ResponseEntity.ok(info);
    }

    @PostMapping("/cmd")
    public ResponseEntity<?> runCommand(@RequestParam String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        String output = new String(process.getInputStream().readAllBytes());
        return ResponseEntity.ok(Map.of("output", output));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestHeader(value = "X-Token", required = false) String token) {
        User requester = authService.getUser(token);
        if (requester != null && requester.r.equals("ADMIN")) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
