package com.cafeteria.api.interno.venta.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemVentaRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad
) {
}
