package com.cafeteria.api.interno.venta.dto;

import java.math.BigDecimal;

public record VentaMostradorResponse(
        Long pedidoId,
        BigDecimal total,
        String estado,
        String codigoRetiro
) {
}
