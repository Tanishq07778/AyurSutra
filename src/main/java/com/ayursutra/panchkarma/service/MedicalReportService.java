package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.entity.MedicalReport;
import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.repository.MedicalReportRepository;
import com.ayursutra.panchkarma.repository.PatientRepository;
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
public class MedicalReportService {

    private final MedicalReportRepository reportRepository;
    private final PatientRepository       patientRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Attach a report to a patient and save it.
     * uploadedAt is set manually (same reason as VitalSign.recordedAt —
     * @CreatedDate fires after transaction commit so the same-request
     * response would have a null timestamp otherwise).
     */
    public MedicalReport uploadReport(Long patientId, MedicalReport report) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Patient not found with id: " + patientId));

        report.setPatient(patient);

        // Guarantee uploadedAt is never null in the response
        if (report.getUploadedAt() == null) {
            report.setUploadedAt(LocalDateTime.now());
        }
        if (report.getReportDate() == null) {
            report.setReportDate(LocalDateTime.now());
        }

        MedicalReport saved = reportRepository.save(report);
        reportRepository.flush();

        log.debug("Report saved — id={} patientId={} type={} title={}",
                saved.getId(), patientId, saved.getReportType(), saved.getTitle());
        return saved;
    }

    public void deleteReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MedicalReport> getPatientReports(Long patientId) {
        return reportRepository.findByPatientIdOrderByUploadedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public MedicalReport getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Report not found with id: " + reportId));
    }
}