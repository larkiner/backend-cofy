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

/**
 * Tabla PEDIDOS desde el lado del EMPLEADO, solo para INSERT
 * (venta de mostrador). CLIENTE_ID queda NULL: es una venta sin
 * cliente registrado, cobrada en el momento por el personal.
 */
@Entity
@Table(name = "PEDIDOS")
@Getter
@Setter
@NoArgsConstructor
public class PedidoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pedidos_venta")
    @SequenceGenerator(name = "seq_pedidos_venta", sequenceName = "CAFETERIA_APP.SEQ_PEDIDOS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SUCURSAL_ID")
    private Long sucursalId;

    @Column(name = "TOTAL")
    private BigDecimal total;
}
