package com.ayursutra.panchkarma.repository;

import com.ayursutra.panchkarma.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    List<Doctor> findByIsAvailableTrue();
    List<Doctor> findBySpecialization(String specialization);

    @Query("SELECT d FROM Doctor d WHERE d.isAvailable = true AND d.status = 'ACTIVE'")
    List<Doctor> findAllAvailableDoctors();

    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
}