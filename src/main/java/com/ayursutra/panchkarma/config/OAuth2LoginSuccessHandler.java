package com.ayursutra.panchkarma.config;

import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.entity.User;
import com.ayursutra.panchkarma.repository.UserRepository;
import com.ayursutra.panchkarma.repository.PatientRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");

        if (firstName == null) firstName = "User";
        if (lastName == null) lastName = "";

        log.info("Google OAuth2 login: email={}, googleId={}", email, googleId);

        try {
            // Find or create user
            User user = findOrCreateUser(email, googleId, firstName, lastName, picture);

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());

            // Get patient ID if applicable
            String patientId = "";
            if (user instanceof Patient) {
                patientId = ((Patient) user).getPatientId() != null
                        ? ((Patient) user).getPatientId()
                        : "AYR-PAT-" + String.format("%06d", user.getId());
            }

            // Redirect to login page with token in URL params
            // Frontend will extract and store in localStorage
            String redirectUrl = String.format(
                    "/login.html?token=%s&userId=%d&email=%s&role=%s&firstName=%s&lastName=%s&patientId=%s",
                    URLEncoder.encode(token, StandardCharsets.UTF_8),
                    user.getId(),
                    URLEncoder.encode(email, StandardCharsets.UTF_8),
                    URLEncoder.encode(user.getRole().name(), StandardCharsets.UTF_8),
                    URLEncoder.encode(firstName, StandardCharsets.UTF_8),
                    URLEncoder.encode(lastName, StandardCharsets.UTF_8),
                    URLEncoder.encode(patientId, StandardCharsets.UTF_8)
            );

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 success handler error", e);
            response.sendRedirect("/login.html?error=oauth_error");
        }
    }

    private User findOrCreateUser(String email, String googleId, String firstName, String lastName, String picture) {
        // Check if user exists by email
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            // Update Google ID and picture if not set
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }
            if (picture != null && user.getProfilePictureUrl() == null) {
                user.setProfilePictureUrl(picture);
            }
            user.setEmailVerified(true);
            return userRepository.save(user);
        }

        // Check by Google ID
        Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingByGoogleId.isPresent()) {
            return existingByGoogleId.get();
        }

        // Create new patient for Google login
        Patient newPatient = new Patient();
        newPatient.setEmail(email);
        newPatient.setFirstName(firstName);
        newPatient.setLastName(lastName);
        newPatient.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
        newPatient.setPhone("0000000000"); // Placeholder - user can update later
        newPatient.setRole(User.Role.PATIENT);
        newPatient.setStatus(User.AccountStatus.ACTIVE);
        newPatient.setGoogleId(googleId);
        newPatient.setProfilePictureUrl(picture);
        newPatient.setEmailVerified(true);
        newPatient.setQrCode("QR-" + UUID.randomUUID().toString());

        // Generate patient ID
        long count = patientRepository.count();
        newPatient.setPatientId(String.format("AYR-PAT-%06d", count + 1));

        log.info("Creating new patient for Google OAuth2 user: {}", email);
        return patientRepository.save(newPatient);
    }
}