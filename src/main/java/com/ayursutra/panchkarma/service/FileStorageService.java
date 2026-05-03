package com.ayursutra.panchkarma.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads/reports}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Created upload directory: {}", dir.toAbsolutePath());
            } else {
                log.info("Upload directory: {}", dir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Cannot create upload directory '{}': {}", uploadDir, e.getMessage());
        }
    }

    /**
     * Save a multipart file to disk using a UUID-based filename so that two
     * uploads of files with the same name do not overwrite each other.
     *
     * @return the relative path stored in the database (e.g. "uploads/reports/uuid.pdf")
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot save empty file");
        }

        // Derive file extension from original filename
        String original  = file.getOriginalFilename();
        String extension = "";
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }

        // Build a UUID filename: <uuid><.ext>
        String uniqueName = UUID.randomUUID().toString() + extension;
        Path target = Paths.get(uploadDir, uniqueName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Saved file '{}' as '{}'", original, target.toAbsolutePath());

        // Return relative path for DB storage
        return uploadDir + "/" + uniqueName;
    }

    /**
     * Delete a file by its relative path (as stored in the DB).
     * Silently succeeds if the file does not exist.
     */
    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Path target = Paths.get(relativePath);
            boolean deleted = Files.deleteIfExists(target);
            if (deleted) log.debug("Deleted file: {}", relativePath);
        } catch (IOException e) {
            log.warn("Could not delete file '{}': {}", relativePath, e.getMessage());
        }
    }
}