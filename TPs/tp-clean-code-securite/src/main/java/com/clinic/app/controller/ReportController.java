package com.clinic.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchExternal(@RequestParam String url) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(Map.of("data", response));
    }

    @GetMapping("/file")
    public ResponseEntity<?> readFile(@RequestParam String path) throws Exception {
        File file = new File(path);
        String content = Files.readString(file.toPath());
        return ResponseEntity.ok(Map.of("content", content));
    }

    @GetMapping("/redirect")
    public ResponseEntity<?> redirect(@RequestParam String target) {
        return ResponseEntity.status(302)
                .header("Location", target)
                .build();
    }
}
