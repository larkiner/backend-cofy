package com.cafeteria.api.interno.trabajador.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta de personal por un ADMIN (POST /api/interno/trabajadores).
 * El rol se elige explícitamente; ya no depende de una contraseña mágica.
 */
public record CrearTrabajadorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String telefono,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "BARISTA|CAJERO|SUPERVISOR|ADMIN",
                 message = "Rol no válido (BARISTA, CAJERO, SUPERVISOR o ADMIN)")
        String rol,

        @NotNull(message = "La sucursal es obligatoria")
        Long sucursalId
) {
}
