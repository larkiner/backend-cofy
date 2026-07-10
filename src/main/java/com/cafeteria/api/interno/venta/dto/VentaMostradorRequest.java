package com.cafeteria.api.interno.venta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record VentaMostradorRequest(
        @NotEmpty @Valid List<ItemVentaRequest> items,

        @NotBlank
        @Pattern(regexp = "TARJETA|PSE|NEQUI|DAVIPLATA|EFECTIVO",
                 message = "Método de pago no válido")
        String metodoPago
) {
}
