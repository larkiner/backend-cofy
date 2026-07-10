package com.cafeteria.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(
        @NotBlank String passwordActual,

        @NotBlank
        @Size(min = 6, max = 100, message = "La nueva contraseña debe tener entre 6 y 100 caracteres")
        String passwordNueva
) {
}
