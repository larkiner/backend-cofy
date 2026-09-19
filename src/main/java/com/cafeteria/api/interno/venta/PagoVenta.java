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
 * Tabla PAGOS desde el lado del EMPLEADO, solo para INSERT.
 * A diferencia del pago en línea del cliente (que nace PENDIENTE
 * y alguien lo aprueba después), este se inserta YA con estado
 * APROBADO: el dinero se cobró en persona en el mostrador. Ese
 * INSERT dispara TRG_PAGOS_APROBADO igual que un UPDATE, así que
 * el código de retiro y el estado PAGADO del pedido se generan
 * de inmediato.
 */
@Entity
@Table(name = "PAGOS")
@Getter
@Setter
@NoArgsConstructor
public class PagoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pagos_venta")
    @SequenceGenerator(name = "seq_pagos_venta", sequenceName = "CAFETERIA_APP.SEQ_PAGOS", allocationSize = 20)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PEDIDO_ID")
    private Long pedidoId;

    @Column(name = "METODO_PAGO")
    private String metodoPago;

    @Column(name = "MONTO")
    private BigDecimal monto;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "REFERENCIA_TRANSACCION")
    private String referenciaTransaccion;
}
