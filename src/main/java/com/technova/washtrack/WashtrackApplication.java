package com.technova.washtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the WashTrack Spring Boot application.
 * Enables JPA auditing support for auditing entity fields (createdAt and updatedAt).
 *
 * @since 1.0
 */
@EnableJpaAuditing
@SpringBootApplication
public class WashtrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(WashtrackApplication.class, args);
	}

}
