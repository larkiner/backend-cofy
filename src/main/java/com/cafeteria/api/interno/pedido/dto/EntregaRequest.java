package com.cafeteria.api.interno.pedido.dto;

import jakarta.validation.constraints.NotBlank;

public record EntregaRequest(
        @NotBlank String codigoRetiro
) {
}
