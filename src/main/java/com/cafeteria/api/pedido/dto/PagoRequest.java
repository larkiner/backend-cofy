package com.cafeteria.api.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PagoRequest(
        @NotBlank
        @Pattern(regexp = "TARJETA|PSE|NEQUI|DAVIPLATA|EFECTIVO",
                 message = "Método de pago no válido")
        String metodo
) {
}
