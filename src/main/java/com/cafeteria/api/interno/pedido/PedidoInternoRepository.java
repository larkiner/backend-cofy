package com.cafeteria.api.interno.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoInternoRepository extends JpaRepository<PedidoInterno, Long> {

    Optional<PedidoInterno> findByCodigoRetiro(String codigoRetiro);
}
