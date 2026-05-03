package com.ayursutra.panchkarma;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AyurSutraApplication {

	public static void main(String[] args) {
		SpringApplication.run(AyurSutraApplication.class, args);
		System.out.println("🌿 AyurSutra Application Started Successfully!");
		System.out.println("📊 Access H2 Console: http://localhost:8080/h2-console");
		System.out.println("📖 API Health Check: http://localhost:8080/api/v1/health");
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
}