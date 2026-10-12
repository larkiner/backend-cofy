package com.cafeteria.api.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Vista VW_DETALLE_PEDIDO_CLIENTE: los ítems de un pedido con el nombre del
 * producto, resuelto en la BD con un JOIN (sin una consulta por ítem).
 * Requiere el Script 11.
 */
@Entity
@Immutable
@Table(name = "VW_DETALLE_PEDIDO_CLIENTE")
@Getter
@NoArgsConstructor
public class DetallePedidoVista {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "PEDIDO_ID")
    private Long pedidoId;

    @Column(name = "PRODUCTO_ID")
    private Long productoId;

    @Column(name = "PRODUCTO_NOMBRE")
    private String productoNombre;

    @Column(name = "CANTIDAD")
    private Integer cantidad;

    @Column(name = "PRECIO_UNITARIO")
    private BigDecimal precioUnitario;

    @Column(name = "SUBTOTAL")
    private BigDecimal subtotal;
}
