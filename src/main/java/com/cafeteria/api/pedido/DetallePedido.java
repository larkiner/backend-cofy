package com.cafeteria.api.pedido;

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

/**
 * Tabla DETALLE_PEDIDO (INSERT + SELECT desde el cliente).
 * SUBTOTAL es una columna virtual de Oracle: solo lectura.
 */
@Entity
@Table(name = "DETALLE_PEDIDO")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_detalle_pedido")
    @SequenceGenerator(name = "seq_detalle_pedido", sequenceName = "CAFETERIA_APP.SEQ_DETALLE_PEDIDO", allocationSize = 1)
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

    @Column(name = "SUBTOTAL", insertable = false, updatable = false)
    private BigDecimal subtotal;
}
