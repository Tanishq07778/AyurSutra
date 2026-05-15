package com.ayursutra.panchkarma.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          @Lazy OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ── Static frontend files — ALL must be public ──────────────
                        // Spring Security was blocking profile.html, doctor-dashboard.html
                        // etc. and redirecting to /login (the Spring default login page,
                        // not our login.html). The browser then showed login.html content
                        // at the wrong URL, and the JS redirect went nowhere.
                        //
                        // FIX: permit ALL .html files and static assets. Auth is handled
                        // entirely in the browser via JWT + localStorage — Spring Security
                        // does NOT need to guard the HTML files themselves.
                        .requestMatchers(
                                "/",
                                "/*.html",          // all HTML pages
                                "/*.js",            // shared.js etc.
                                "/*.css",
                                "/images/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ── Public API endpoints ────────────────────────────────────
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/patients/register").permitAll()
                        .requestMatchers("/api/v1/doctors/register").permitAll()
                        .requestMatchers("/api/v1/admins/register").permitAll()
                        .requestMatchers("/api/v1/health", "/api/v1/welcome").permitAll()

                        // ── H2 console (dev only) ───────────────────────────────────
                        .requestMatchers("/h2-console/**").permitAll()

                        // ── OAuth2 endpoints ────────────────────────────────────────
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // ── Everything else (API calls) requires JWT ────────────────
                        .anyRequest().authenticated()
                )
                // Session: stateless for API, but IF_REQUIRED for OAuth2 flow
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // Allow H2 iframe
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )
                // JWT filter runs before username/password auth
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl("/login.html?error=oauth_failed")
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:8080",
                "http://127.0.0.1:8080"
        ));
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}