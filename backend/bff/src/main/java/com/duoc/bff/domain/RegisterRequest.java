package com.duoc.bff.domain;

/** Datos de registro de un usuario nuevo. role = perfil de plataforma (DEV/PM/DIR). */
public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String rut,
        String password,
        String role
) {}
