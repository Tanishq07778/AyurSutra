package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.entity.VitalSign;
import com.ayursutra.panchkarma.repository.PatientRepository;
import com.ayursutra.panchkarma.repository.VitalSignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VitalSignService {

    private final VitalSignRepository vitalSignRepository;
    private final PatientRepository   patientRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Write operations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Record a new set of vitals for a patient.
     *
     * FIX 1 – recordedAt null in response:
     *   @CreatedDate is populated by the JPA AuditingEntityListener during the
     *   EntityManager flush that happens at transaction commit time — AFTER this
     *   method returns.  If the caller reads recordedAt from the returned object
     *   before the transaction commits it will be null, causing the frontend
     *   date display to break and the chart to receive null timestamps.
     *   Solution: set recordedAt explicitly before save(), then call flush() so
     *   Hibernate writes the row immediately and the returned entity is complete.
     *
     * FIX 2 – patient not found produces NullPointerException instead of 404:
     *   Using orElseThrow() gives a clear message that the controller can catch.
     */
    public VitalSign recordVitals(Long patientId, VitalSign vitalSign) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Patient not found with id: " + patientId));

        vitalSign.setPatient(patient);

        // Always set recordedAt before save so the value is present in the
        // returned object within the same HTTP request.
        if (vitalSign.getRecordedAt() == null) {
            vitalSign.setRecordedAt(LocalDateTime.now());
        }

        // Calculate BMI from the patient's stored height
        vitalSign.calculateBMI();

        VitalSign saved = vitalSignRepository.save(vitalSign);

        // flush() makes Hibernate execute the INSERT immediately so that the
        // auto-generated ID and any @CreatedDate value are populated on `saved`.
        vitalSignRepository.flush();

        log.debug("Vitals saved — patientId={} bp={} hr={} recordedAt={}",
                patientId, saved.getBpString(), saved.getHeartRate(), saved.getRecordedAt());

        return saved;
    }

    public void deleteVital(Long vitalId) {
        vitalSignRepository.deleteById(vitalId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read operations
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VitalSign> getPatientVitals(Long patientId) {
        // FIX: use patient_Id (underscore) to traverse the @ManyToOne relationship
        List<VitalSign> vitals =
                vitalSignRepository.findByPatient_IdOrderByRecordedAtDesc(patientId);
        log.debug("Fetched {} vitals for patientId={}", vitals.size(), patientId);
        return vitals;
    }

    @Transactional(readOnly = true)
    public VitalSign getLatestVitals(Long patientId) {
        List<VitalSign> vitals =
                vitalSignRepository.findLatestByPatientId(patientId);
        return vitals.isEmpty() ? null : vitals.get(0);
    }

    @Transactional(readOnly = true)
    public List<VitalSign> getVitalsByDateRange(Long patientId,
                                                LocalDateTime startDate,
                                                LocalDateTime endDate) {
        return vitalSignRepository.findByPatientIdAndDateRange(
                patientId, startDate, endDate);
    }
}