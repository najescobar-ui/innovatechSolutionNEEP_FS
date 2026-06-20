package com.duoc.bff.web;

import com.duoc.bff.domain.RegisterRequest;
import com.duoc.bff.service.KeycloakAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/** Registro de cuentas. Endpoint publico (no requiere JWT). */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Set<String> VALID_ROLES = Set.of("DEV", "PM", "DIR");

    private final KeycloakAdminService keycloak;

    public AuthController(KeycloakAdminService keycloak) {
        this.keycloak = keycloak;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@RequestBody RegisterRequest req) {
        if (req.email() == null || req.email().isBlank()) {
            throw new IllegalArgumentException("email es requerido");
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("password es requerido");
        }
        if (req.role() == null || !VALID_ROLES.contains(req.role())) {
            throw new IllegalArgumentException("perfil invalido; use DEV, PM o DIR");
        }
        keycloak.register(req);
        return Map.of("status", "created", "email", req.email(), "role", req.role());
    }
}
