package com.example.demo_jour_3.controller;

import com.example.demo_jour_3.demo_aspect.aspect.PerformanceAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Autowired
    private PerformanceAspect performanceAspect;

    @GetMapping
    public Map<String, Object> getMetrics() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("endpoints", performanceAspect.getMetrics());
        response.put("totalEndpoints", performanceAspect.getMetrics().size());
        return response;
    }

    @DeleteMapping
    public Map<String, String> resetMetrics() {
        performanceAspect.resetMetrics();
        return Map.of(
                "status", "ok",
                "message", "Toutes les métriques ont été remises à zéro"
        );
    }
}