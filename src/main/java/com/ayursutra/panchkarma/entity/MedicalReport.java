package com.ayursutra.panchkarma.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_reports")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MedicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    /*
     * FIX — H2 migration error:
     *   "NULL not allowed for column TITLE"
     *
     *   The medical_reports table already existed in the file-based H2 DB
     *   without a TITLE column.  When Hibernate tries to ALTER the table to
     *   add  title VARCHAR NOT NULL  it copies all existing rows first — but
     *   those rows have no title value, so H2 rejects them with a NOT NULL
     *   constraint violation.
     *
     *   Fix: remove nullable=false so Hibernate adds the column as nullable.
     *   Existing rows get NULL for title, new uploads always supply a title
     *   (enforced by the controller @RequestParam("title") required=true).
     *
     *   If you want NOT NULL enforced at DB level in future, delete the
     *   ./data/ayursutra_db.* files once and let Hibernate recreate the schema
     *   clean, then add nullable=false back.
     */
    @Column(name = "title")          // nullable by default — intentional, see above
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type")    // also nullable — same migration reason
    private ReportType reportType;

    @Column(length = 1000)
    private String description;

    @Column(name = "report_date")
    private LocalDateTime reportDate;

    @CreatedDate
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "doctor_notes", length = 2000)
    private String doctorNotes;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    public enum ReportType {
        LAB_REPORT,
        RADIOLOGY,
        PRESCRIPTION,
        DISCHARGE_SUMMARY,
        CONSULTATION_NOTES,
        OTHER
    }
}