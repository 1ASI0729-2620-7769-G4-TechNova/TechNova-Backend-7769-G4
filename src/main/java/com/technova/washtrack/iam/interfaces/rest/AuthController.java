package com.technova.washtrack.iam.interfaces.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Demo login: Identity & Access is out of scope for this deliverable, so a single fixed account stands in for it. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Demo login (username demo / password demo123)")
public class AuthController {

    private static final String DEMO_USER = "demo";
    private static final String DEMO_PASSWORD = "demo123";

    @PostMapping("/login")
    @Operation(summary = "Log in with the demo account")
    Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        if (!DEMO_USER.equals(request.username()) || !DEMO_PASSWORD.equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return Map.of("username", DEMO_USER, "token", "demo-token");
    }
}
