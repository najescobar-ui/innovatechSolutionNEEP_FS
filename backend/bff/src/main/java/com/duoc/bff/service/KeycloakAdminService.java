package com.duoc.bff.service;

import com.duoc.bff.domain.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Crea cuentas en Keycloak via Admin REST API (Keycloak es el almacen de
 * identidades). Flujo: token de admin en el realm master -> crear usuario con
 * atributo rut -> asignar el realm role del perfil elegido.
 */
@Service
public class KeycloakAdminService {

    private final RestClient http;
    private final String realm;
    private final String adminRealm;
    private final String clientId;
    private final String username;
    private final String password;

    public KeycloakAdminService(
            @Value("${keycloak.admin.base-url}") String baseUrl,
            @Value("${keycloak.admin.realm}") String realm,
            @Value("${keycloak.admin.admin-realm}") String adminRealm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.username}") String username,
            @Value("${keycloak.admin.password}") String password) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
        this.realm = realm;
        this.adminRealm = adminRealm;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    public void register(RegisterRequest req) {
        var token = adminToken();
        createUser(token, req);
        var userId = findUserId(token, req.email());
        assignRealmRole(token, userId, req.role());
    }

    /** Token de servicio del admin (realm master, client admin-cli, password grant). */
    private String adminToken() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", password);
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = http.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", adminRealm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
        if (resp == null || resp.get("access_token") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo autenticar contra Keycloak admin");
        }
        return (String) resp.get("access_token");
    }

    private void createUser(String token, RegisterRequest req) {
        var body = Map.of(
                "username", req.email(),
                "email", req.email(),
                "firstName", req.firstName() != null ? req.firstName() : "",
                "lastName", req.lastName() != null ? req.lastName() : "",
                "enabled", true,
                "emailVerified", true,
                "attributes", Map.of("rut", List.of(req.rut() != null ? req.rut() : "")),
                "credentials", List.of(Map.of(
                        "type", "password", "value", req.password(), "temporary", false))
        );
        try {
            http.post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya esta registrado");
        }
    }

    private String findUserId(String token, String email) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = http.get()
                .uri(uri -> uri.path("/admin/realms/{realm}/users")
                        .queryParam("email", email)
                        .queryParam("exact", true)
                        .build(realm))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(List.class);
        if (users == null || users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario creado pero no encontrado");
        }
        return (String) users.get(0).get("id");
    }

    private void assignRealmRole(String token, String userId, String role) {
        @SuppressWarnings("unchecked")
        Map<String, Object> roleRep = http.get()
                .uri("/admin/realms/{realm}/roles/{role}", realm, role)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);
        http.post()
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", realm, userId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRep))
                .retrieve()
                .toBodilessEntity();
    }
}
