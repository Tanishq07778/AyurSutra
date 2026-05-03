package com.ayursutra.panchkarma.repository;

import com.ayursutra.panchkarma.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {

    /** All reports for a patient, newest first. */
    List<MedicalReport> findByPatientIdOrderByUploadedAtDesc(Long patientId);
}