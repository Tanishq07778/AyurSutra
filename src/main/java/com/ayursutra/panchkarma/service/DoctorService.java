package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.dto.DoctorRegistrationRequest;
import com.ayursutra.panchkarma.dto.UserResponse;
import com.ayursutra.panchkarma.entity.Doctor;
import com.ayursutra.panchkarma.entity.User;
import com.ayursutra.panchkarma.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerDoctor(DoctorRegistrationRequest request) {
        // Check if email already exists
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if license number already exists
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already registered");
        }

        // Create new doctor

        Doctor doctor = new Doctor();
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctor.setPhone(request.getPhone());
        doctor.setRole(User.Role.DOCTOR);
        doctor.setStatus(User.AccountStatus.ACTIVE);

        // Set doctor-specific fields
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setExpertiseAreas(request.getExpertiseAreas());
        doctor.setAvailableFrom(request.getAvailableFrom());
        doctor.setAvailableTo(request.getAvailableTo());
        doctor.setWorkingDays(request.getWorkingDays());
        doctor.setBio(request.getBio());
        doctor.setIsAvailable(true);

// Generate unique doctor ID
        doctor.setDoctorId(generateDoctorId());  // ADD THIS LINE

        Doctor savedDoctor = doctorRepository.save(doctor);
        return com.ayursutra.panchkarma.dto.UserResponse.from(savedDoctor);
    }

    public UserResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + id));
        return com.ayursutra.panchkarma.dto.UserResponse.from(doctor);
    }

    public Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + id));
    }

    public List<UserResponse> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(com.ayursutra.panchkarma.dto.UserResponse::from)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAvailableDoctors() {
        return doctorRepository.findAllAvailableDoctors()
                .stream()
                .map(com.ayursutra.panchkarma.dto.UserResponse::from)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(com.ayursutra.panchkarma.dto.UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse updateDoctor(Long id, DoctorRegistrationRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + id));

        // Update basic info
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setPhone(request.getPhone());

        // Update doctor-specific fields
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setExpertiseAreas(request.getExpertiseAreas());
        doctor.setAvailableFrom(request.getAvailableFrom());
        doctor.setAvailableTo(request.getAvailableTo());
        doctor.setWorkingDays(request.getWorkingDays());
        doctor.setBio(request.getBio());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return com.ayursutra.panchkarma.dto.UserResponse.from(updatedDoctor);
    }

    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new IllegalArgumentException("Doctor not found with id: " + id);
        }
        doctorRepository.deleteById(id);
    }

    public void updateAvailability(Long id, boolean isAvailable) {
        Doctor doctor = getDoctorEntityById(id);
        doctor.setIsAvailable(isAvailable);
        doctorRepository.save(doctor);
    }

    // Add this method:
    private String generateDoctorId() {
        // Generate format: AYR-DOC-XXXXXX
        long count = doctorRepository.count();
        return String.format("AYR-DOC-%06d", count + 1);
    }
}