package com.cafeteria.api.pedido.dto;

import java.math.BigDecimal;

public record PedidoCreadoResponse(
        Long pedidoId,
        BigDecimal total,
        String estado
) {
}
