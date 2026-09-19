package com.cafeteria.api.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que el cliente autenticado puede actualizar de su perfil.
 * El email (identidad de login) y la contraseña no se cambian aquí:
 * la contraseña tiene su propio endpoint (PUT /api/auth/password).
 */
public record ActualizarClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener exactamente 10 dígitos")
        String telefono
) {
}
