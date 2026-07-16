package com.cafeteria.api.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoClienteVistaRepository extends JpaRepository<PedidoClienteVista, Long> {

    List<PedidoClienteVista> findByClienteIdOrderByFechaPedidoDesc(Long clienteId);

    /**
     * Historial de compras: solo los pedidos que el cliente llegó a pagar
     * (se excluye el estado indicado, típicamente PENDIENTE_PAGO), del más
     * reciente al más antiguo.
     */
    List<PedidoClienteVista> findByClienteIdAndEstadoNotOrderByFechaPedidoDesc(
            Long clienteId, String estado);
}
