package com.cafeteria.api.pedido.dto;

import com.cafeteria.api.pedido.DetallePedido;
import com.cafeteria.api.pedido.PedidoClienteVista;

import java.util.List;

public record PedidoDetalleResponse(
        PedidoClienteVista pedido,
        List<DetallePedido> items
) {
}
