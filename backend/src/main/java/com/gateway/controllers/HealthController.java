package com.gateway.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application and dependencies.
 * Checks database connectivity and includes placeholders for redis/worker status.
 */
@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get application health status.
     * Checks database connectivity. Returns status, timestamp, and service statuses.
     * @return ResponseEntity with status, timestamp, database, redis, and worker fields
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "healthy");
        body.put("timestamp", Instant.now().toString());

        try {
            jdbcTemplate.execute("SELECT 1");
            body.put("database", "connected");
        } catch (Exception ex) {
            body.put("database", "disconnected");
        }

        // Placeholder for redis check (Deliverable 2)
        body.put("redis", "connected");

        // Placeholder for worker status check (Deliverable 2)
        body.put("worker", "running");

        return ResponseEntity.ok(body);
    }
}
