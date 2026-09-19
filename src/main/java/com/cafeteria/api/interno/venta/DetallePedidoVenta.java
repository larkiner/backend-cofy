package com.cafeteria.api.interno.venta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Tabla DETALLE_PEDIDO desde el lado del EMPLEADO, solo para INSERT. */
@Entity
@Table(name = "DETALLE_PEDIDO")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedidoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_detalle_pedido_venta")
    @SequenceGenerator(name = "seq_detalle_pedido_venta", sequenceName = "CAFETERIA_APP.SEQ_DETALLE_PEDIDO", allocationSize = 20)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PEDIDO_ID")
    private Long pedidoId;

    @Column(name = "PRODUCTO_ID")
    private Long productoId;

    @Column(name = "CANTIDAD")
    private Integer cantidad;

    @Column(name = "PRECIO_UNITARIO")
    private BigDecimal precioUnitario;
}
