package com.cafeteria.api.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoClienteVistaRepository extends JpaRepository<PedidoClienteVista, Long> {

    List<PedidoClienteVista> findByClienteIdOrderByFechaPedidoDesc(Long clienteId);
}
