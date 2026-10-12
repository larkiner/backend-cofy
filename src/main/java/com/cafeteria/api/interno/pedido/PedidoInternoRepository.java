package com.cafeteria.api.interno.pedido;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PedidoInternoRepository extends JpaRepository<PedidoInterno, Long> {

    Optional<PedidoInterno> findByCodigoRetiro(String codigoRetiro);

    /** Serializa cambios de estado y entrega sobre el mismo pedido. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoInterno p where p.id = :id")
    Optional<PedidoInterno> findByIdForUpdate(@Param("id") Long id);

    /** Evita que dos cajeros entreguen el mismo código simultáneamente. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoInterno p where p.codigoRetiro = :codigoRetiro")
    Optional<PedidoInterno> findByCodigoRetiroForUpdate(@Param("codigoRetiro") String codigoRetiro);

    /**
     * Cancela los pedidos que siguen en PENDIENTE_PAGO desde antes de
     * {@code limite} y que nunca iniciaron un pago (no tienen fila en PAGOS).
     * Los que ya tienen un pago (p. ej. un PaymentIntent de Stripe pendiente)
     * no se tocan: todavía pueden pagarse y el trigger TRG_PAGOS_APROBADO los
     * pasaría a PAGADO. Devuelve cuántos se cancelaron.
     */
    @Modifying
    @Transactional("empleadoTransactionManager")
    @Query("""
            update PedidoInterno p
               set p.estado = 'CANCELADO'
             where p.estado = 'PENDIENTE_PAGO'
               and p.fechaPedido < :limite
               and not exists (select 1 from PagoInterno g where g.pedidoId = p.id)
            """)
    int cancelarPendientesSinPago(@Param("limite") LocalDateTime limite);
}
