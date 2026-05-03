package com.ayursutra.panchkarma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "AyurSutra - Panchakarma Management System");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "🌿 System is running perfectly!");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Welcome to AyurSutra - Panchakarma Management System! 🙏");
    }
}
