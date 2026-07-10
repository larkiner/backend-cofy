package com.cafeteria.api.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByPedidoId(Long pedidoId);

    Optional<Pago> findByPedidoId(Long pedidoId);
}
