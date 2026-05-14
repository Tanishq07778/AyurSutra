package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.entity.Connection;
import com.ayursutra.panchkarma.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/connections")
@RequiredArgsConstructor
@Slf4j
public class ConnectionController {

    private final ConnectionService connectionService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<Connection>>> getPatientConnections(@PathVariable Long patientId) {
        List<Connection> list = connectionService.getPatientConnections(patientId);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + list.size() + " connections", list));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<Connection>>> getDoctorConnections(@PathVariable Long doctorId) {
        List<Connection> list = connectionService.getDoctorConnections(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + list.size() + " connections", list));
    }

    @GetMapping("/doctor/{doctorId}/pending")
    public ResponseEntity<ApiResponse<List<Connection>>> getPending(@PathVariable Long doctorId) {
        List<Connection> list = connectionService.getDoctorPendingRequests(doctorId);
        return ResponseEntity.ok(ApiResponse.success(list.size() + " pending", list));
    }

    @GetMapping("/doctor/{doctorId}/patients")
    public ResponseEntity<ApiResponse<List<Connection>>> getConnectedPatients(@PathVariable Long doctorId) {
        List<Connection> list = connectionService.getDoctorConnectedPatients(doctorId);
        return ResponseEntity.ok(ApiResponse.success(list.size() + " patients", list));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getStatus(
            @RequestParam Long patientId, @RequestParam Long doctorId) {
        Optional<Connection> conn = connectionService.getConnection(patientId, doctorId);
        if (conn.isEmpty())
            return ResponseEntity.ok(ApiResponse.success("No connection", Map.of("status","NONE","connectionId",-1)));
        Connection c = conn.get();
        return ResponseEntity.ok(ApiResponse.success("Found", Map.of(
                "status", c.getStatus().name(),
                "connectionId", c.getId(),
                "requestedBy", c.getRequestedBy() != null ? c.getRequestedBy() : "")));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Connection>> sendRequest(@RequestBody Map<String,Object> body) {
        try {
            Long patientId = Long.parseLong(body.get("patientId").toString());
            Long doctorId  = Long.parseLong(body.get("doctorId").toString());
            String message = body.getOrDefault("message","").toString();
            Connection conn = connectionService.sendRequest(patientId, doctorId, message);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Request sent!", conn));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("sendRequest error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Connection>> accept(
            @PathVariable Long id, @RequestBody Map<String,Object> body) {
        try {
            Long doctorId = Long.parseLong(body.get("doctorId").toString());
            return ResponseEntity.ok(ApiResponse.success("Accepted!", connectionService.acceptRequest(id, doctorId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Connection>> reject(
            @PathVariable Long id, @RequestBody Map<String,Object> body) {
        try {
            Long doctorId = Long.parseLong(body.get("doctorId").toString());
            return ResponseEntity.ok(ApiResponse.success("Declined.", connectionService.rejectRequest(id, doctorId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> disconnect(@PathVariable Long id) {
        connectionService.disconnect(id);
        return ResponseEntity.ok(ApiResponse.success("Disconnected", null));
    }
}