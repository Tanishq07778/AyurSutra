package com.ayursutra.panchkarma.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id","doctor_id"}))
@Data
@NoArgsConstructor
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"password","currentMedications","allergies","hibernateLazyInitializer"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties({"password","hibernateLazyInitializer"})
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus status = ConnectionStatus.PENDING;

    @Column(name = "requested_by")
    private String requestedBy = "PATIENT";

    @Column(name = "request_message", length = 500)
    private String requestMessage;

    // FIX: set manually in service (same @CreatedDate timing issue)
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    public enum ConnectionStatus { PENDING, CONNECTED, REJECTED }
}