package com.ayursutra.panchkarma.repository;

import com.ayursutra.panchkarma.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByPhone(String phone);
    Optional<Patient> findByQrCode(String qrCode);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}