package com.ayursutra.panchkarma.repository;

import com.ayursutra.panchkarma.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
}