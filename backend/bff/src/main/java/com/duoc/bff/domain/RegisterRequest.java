package com.duoc.bff.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Datos de registro de un usuario nuevo. role = perfil de plataforma (DEV/PM/DIR). */
public record RegisterRequest(
        @NotBlank(message = "El nombre es requerido")
        String firstName,

        @NotBlank(message = "El apellido es requerido")
        String lastName,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no es válido")
        String email,

        @NotBlank(message = "El RUT es requerido")
        @Pattern(regexp = "^\\d{1,3}(\\.\\d{3})*-[\\dkK]$",
                message = "El RUT no tiene un formato válido (ej: 12.345.678-9)")
        String rut,

        // Minimo 8, con mayuscula, minuscula y al menos un caracter especial.
        @NotBlank(message = "La contraseña es requerida")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "La contraseña debe tener mínimo 8 caracteres, con mayúscula, minúscula y un carácter especial")
        String password,

        @NotBlank(message = "El perfil es requerido")
        @Pattern(regexp = "DEV|PM|DIR", message = "Perfil inválido; use DEV, PM o DIR")
        String role
) {}
