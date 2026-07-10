package com.cafeteria.api.interno.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tabla PAGOS desde el PANEL INTERNO (SELECT + UPDATE).
 * Aquí se aprueba/rechaza el pago (pasarela simulada o cajero).
 * Al pasar a APROBADO, el trigger de Oracle genera el código de retiro.
 */
@Entity
@Table(name = "PAGOS")
@Getter
@Setter
@NoArgsConstructor
public class PagoInterno {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "PEDIDO_ID", updatable = false)
    private Long pedidoId;

    @Column(name = "METODO_PAGO", updatable = false)
    private String metodoPago;

    @Column(name = "MONTO", updatable = false)
    private BigDecimal monto;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "REFERENCIA_TRANSACCION")
    private String referenciaTransaccion;

    @Column(name = "FECHA_PAGO", updatable = false)
    private LocalDateTime fechaPago;
}
