package com.cafeteria.api.interno.pedido;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
