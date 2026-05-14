package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.entity.Connection;
import com.ayursutra.panchkarma.entity.Connection.ConnectionStatus;
import com.ayursutra.panchkarma.entity.Doctor;
import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.repository.ConnectionRepository;
import com.ayursutra.panchkarma.repository.DoctorRepository;
import com.ayursutra.panchkarma.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConnectionService {

    private final ConnectionRepository connectionRepo;
    private final PatientRepository    patientRepo;
    private final DoctorRepository     doctorRepo;

    public Connection sendRequest(Long patientId, Long doctorId, String message) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

        Optional<Connection> existing = connectionRepo.findByPatient_IdAndDoctor_Id(patientId, doctorId);
        if (existing.isPresent()) {
            Connection c = existing.get();
            if (c.getStatus() == ConnectionStatus.CONNECTED)
                throw new IllegalStateException("Already connected to this doctor");
            if (c.getStatus() == ConnectionStatus.PENDING)
                throw new IllegalStateException("Request already pending");
            // Allow re-request after rejection
            c.setStatus(ConnectionStatus.PENDING);
            c.setRequestMessage(message);
            c.setRequestedAt(LocalDateTime.now());
            c.setRespondedAt(null);
            c.setConnectedAt(null);
            return connectionRepo.save(c);
        }

        Connection conn = new Connection();
        conn.setPatient(patient);
        conn.setDoctor(doctor);
        conn.setStatus(ConnectionStatus.PENDING);
        conn.setRequestedBy("PATIENT");
        conn.setRequestMessage(message);
        conn.setRequestedAt(LocalDateTime.now());
        return connectionRepo.save(conn);
    }

    public Connection acceptRequest(Long connectionId, Long doctorId) {
        Connection conn = connectionRepo.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));
        if (!conn.getDoctor().getId().equals(doctorId))
            throw new IllegalStateException("Not authorized");
        conn.setStatus(ConnectionStatus.CONNECTED);
        conn.setRespondedAt(LocalDateTime.now());
        conn.setConnectedAt(LocalDateTime.now());
        return connectionRepo.save(conn);
    }

    public Connection rejectRequest(Long connectionId, Long doctorId) {
        Connection conn = connectionRepo.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));
        if (!conn.getDoctor().getId().equals(doctorId))
            throw new IllegalStateException("Not authorized");
        conn.setStatus(ConnectionStatus.REJECTED);
        conn.setRespondedAt(LocalDateTime.now());
        return connectionRepo.save(conn);
    }

    public void disconnect(Long connectionId) {
        connectionRepo.deleteById(connectionId);
    }

    @Transactional(readOnly = true)
    public List<Connection> getPatientConnections(Long patientId) {
        return connectionRepo.findByPatient_IdOrderByRequestedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Connection> getDoctorConnections(Long doctorId) {
        return connectionRepo.findByDoctor_IdOrderByRequestedAtDesc(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Connection> getDoctorPendingRequests(Long doctorId) {
        return connectionRepo.findByDoctor_IdAndStatusOrderByRequestedAtDesc(doctorId, ConnectionStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Connection> getDoctorConnectedPatients(Long doctorId) {
        return connectionRepo.findByDoctor_IdAndStatusOrderByRequestedAtDesc(doctorId, ConnectionStatus.CONNECTED);
    }

    @Transactional(readOnly = true)
    public Optional<Connection> getConnection(Long patientId, Long doctorId) {
        return connectionRepo.findByPatient_IdAndDoctor_Id(patientId, doctorId);
    }

    @Transactional(readOnly = true)
    public boolean isConnected(Long patientId, Long doctorId) {
        return connectionRepo.findByPatient_IdAndDoctor_Id(patientId, doctorId)
                .map(c -> c.getStatus() == ConnectionStatus.CONNECTED)
                .orElse(false);
    }
}