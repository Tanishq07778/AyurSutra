package com.ayursutra.panchkarma.repository;

import com.ayursutra.panchkarma.entity.VitalSign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {

    /*
     * FIX — startup crash:
     *   findByPatientId() fails because VitalSign has no field named "patientId".
     *   It has a @ManyToOne field named "patient" (type Patient).
     *
     *   Spring Data derived queries traverse relationships using the underscore
     *   separator:  patient_Id  means  VitalSign.patient → Patient.id
     *
     *   WRONG:   findByPatientId(...)          → looks for field "patientId"  ❌
     *   CORRECT: findByPatient_Id(...)         → traverses patient.id         ✅
     *
     *   The two @Query methods below already use v.patient.id directly in JPQL
     *   so they are fine as-is.
     */

    /** All vitals for a patient, newest first. Used by dashboard grid + chart. */
    List<VitalSign> findByPatient_IdOrderByRecordedAtDesc(Long patientId);

    /** Used by VitalSignService.getLatestVitals() — caller takes .get(0). */
    @Query("SELECT v FROM VitalSign v WHERE v.patient.id = :patientId ORDER BY v.recordedAt DESC")
    List<VitalSign> findLatestByPatientId(@Param("patientId") Long patientId);

    /** Date-range filter for 7D / 30D / 90D chart buttons. */
    @Query("SELECT v FROM VitalSign v " +
            "WHERE v.patient.id = :patientId " +
            "  AND v.recordedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY v.recordedAt DESC")
    List<VitalSign> findByPatientIdAndDateRange(
            @Param("patientId")  Long          patientId,
            @Param("startDate")  LocalDateTime startDate,
            @Param("endDate")    LocalDateTime endDate);
}