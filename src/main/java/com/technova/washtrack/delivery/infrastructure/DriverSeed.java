package com.technova.washtrack.delivery.infrastructure;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.technova.washtrack.delivery.application.DriverService;

/** Demo drivers so a fresh (in-memory) deployment can schedule deliveries right away. */
@Configuration
class DriverSeed {

    @Bean
    ApplicationRunner seedDrivers(DriverService drivers) {
        return args -> {
            if (drivers.list(false).isEmpty()) {
                drivers.register("Luis Quispe", "987654321", "Moto Honda 150cc - ABC-123");
                drivers.register("Carmen Rojas", "976543210", "Furgoneta Hyundai H100 - DEF-456");
                drivers.register("Jorge Salas", "965432109", "Moto Yamaha 125cc - GHI-789");
            }
        };
    }
}
