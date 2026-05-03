package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.dto.PatientRegistrationRequest;
import com.ayursutra.panchkarma.dto.PatientUpdateRequest;
import com.ayursutra.panchkarma.dto.UserResponse;
import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.entity.User;
import com.ayursutra.panchkarma.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerPatient(PatientRegistrationRequest request) {
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (patientRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setPhone(request.getPhone());
        patient.setRole(User.Role.PATIENT);
        patient.setStatus(User.AccountStatus.ACTIVE);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setDoshaType(request.getDoshaType());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setCurrentMedications(request.getCurrentMedications());
        patient.setHeightCm(request.getHeightCm());
        patient.setWeightKg(request.getWeightKg());
        patient.setQrCode("QR-" + UUID.randomUUID());
        patient.setPatientId(generatePatientId());

        return UserResponse.from(patientRepository.save(patient));
    }

    /**
     * Edit Profile — only updates non-null fields, preserves email/password/ID.
     */
    public UserResponse updatePatientProfile(Long id, PatientUpdateRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));

        if (request.getFirstName() != null)              patient.setFirstName(request.getFirstName());
        if (request.getLastName() != null)               patient.setLastName(request.getLastName());
        if (request.getPhone() != null)                  patient.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null)            patient.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null)                 patient.setGender(request.getGender());
        if (request.getAddress() != null)                patient.setAddress(request.getAddress());
        if (request.getEmergencyContactName() != null)   patient.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null)  patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getBloodGroup() != null)             patient.setBloodGroup(request.getBloodGroup());
        if (request.getDoshaType() != null)              patient.setDoshaType(request.getDoshaType());
        if (request.getMedicalHistory() != null)         patient.setMedicalHistory(request.getMedicalHistory());
        if (request.getAllergies() != null)               patient.setAllergies(request.getAllergies());
        if (request.getCurrentMedications() != null)     patient.setCurrentMedications(request.getCurrentMedications());
        if (request.getHeightCm() != null)               patient.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null)               patient.setWeightKg(request.getWeightKg());

        return UserResponse.from(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public UserResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));
        return UserResponse.from(patient);
    }

    @Transactional(readOnly = true)
    public Patient getPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse updatePatient(Long id, PatientRegistrationRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setDoshaType(request.getDoshaType());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setCurrentMedications(request.getCurrentMedications());
        patient.setHeightCm(request.getHeightCm());
        patient.setWeightKg(request.getWeightKg());
        return UserResponse.from(patientRepository.save(patient));
    }

    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Patient getPatientByQRCode(String qrCode) {
        return patientRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with QR code: " + qrCode));
    }

    private String generatePatientId() {
        long count = patientRepository.count();
        return String.format("AYR-PAT-%06d", count + 1);
    }
}