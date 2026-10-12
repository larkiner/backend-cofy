package com.cafeteria.api.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoVistaRepository extends JpaRepository<DetallePedidoVista, Long> {

    List<DetallePedidoVista> findByPedidoIdOrderById(Long pedidoId);
}
