package com.cafeteria.api.interno.pedido;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PagoInternoRepository extends JpaRepository<PagoInterno, Long> {

    Optional<PagoInterno> findByPedidoId(Long pedidoId);

    /** Una sola aprobación puede procesar un pago pendiente. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PagoInterno p where p.pedidoId = :pedidoId")
    Optional<PagoInterno> findByPedidoIdForUpdate(@Param("pedidoId") Long pedidoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PagoInterno p where p.referenciaTransaccion = :referencia")
    Optional<PagoInterno> findByReferenciaTransaccionForUpdate(@Param("referencia") String referencia);
}
