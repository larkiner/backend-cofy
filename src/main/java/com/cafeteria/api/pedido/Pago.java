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
import java.time.LocalDateTime;

/**
 * Tabla PAGOS desde el lado del CLIENTE (INSERT + SELECT).
 * El cliente crea el pago en PENDIENTE; la aprobación la hace
 * la "pasarela" (simulada por el backend con el datasource interno).
 */
@Entity
@Table(name = "PAGOS")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pagos")
    @SequenceGenerator(name = "seq_pagos", sequenceName = "CAFETERIA_APP.SEQ_PAGOS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PEDIDO_ID")
    private Long pedidoId;

    @Column(name = "METODO_PAGO")
    private String metodoPago;

    @Column(name = "MONTO")
    private BigDecimal monto;

    @Column(name = "ESTADO", insertable = false, updatable = false)
    private String estado;

    @Column(name = "REFERENCIA_TRANSACCION", insertable = false, updatable = false)
    private String referenciaTransaccion;

    @Column(name = "FECHA_PAGO", insertable = false, updatable = false)
    private LocalDateTime fechaPago;
}
