package com.ayursutra.panchkarma.repository;

import com.ayursutra.panchkarma.entity.Connection;
import com.ayursutra.panchkarma.entity.Connection.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findByPatient_IdOrderByRequestedAtDesc(Long patientId);
    List<Connection> findByDoctor_IdOrderByRequestedAtDesc(Long doctorId);
    List<Connection> findByDoctor_IdAndStatusOrderByRequestedAtDesc(Long doctorId, ConnectionStatus status);
    List<Connection> findByPatient_IdAndStatusOrderByRequestedAtDesc(Long patientId, ConnectionStatus status);
    Optional<Connection> findByPatient_IdAndDoctor_Id(Long patientId, Long doctorId);
    long countByDoctor_IdAndStatus(Long doctorId, ConnectionStatus status);
}