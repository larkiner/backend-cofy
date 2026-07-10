package com.cafeteria.api.interno.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoInternoRepository extends JpaRepository<PagoInterno, Long> {

    Optional<PagoInterno> findByPedidoId(Long pedidoId);
}
