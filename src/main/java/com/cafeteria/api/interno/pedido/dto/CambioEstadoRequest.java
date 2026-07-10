package com.cafeteria.api.interno.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioEstadoRequest(
        @NotBlank
        @Pattern(regexp = "EN_PREPARACION|LISTO",
                 message = "Solo se permite cambiar a EN_PREPARACION o LISTO")
        String estado
) {
}
