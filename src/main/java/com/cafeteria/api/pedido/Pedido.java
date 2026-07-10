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
 * Tabla PEDIDOS desde el lado del CLIENTE (INSERT + SELECT).
 * El estado inicial (PENDIENTE_PAGO), la fecha y el código de retiro
 * los pone la BD (defaults y trigger TRG_PAGOS_APROBADO).
 */
@Entity
@Table(name = "PEDIDOS")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pedidos")
    @SequenceGenerator(name = "seq_pedidos", sequenceName = "CAFETERIA_APP.SEQ_PEDIDOS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CLIENTE_ID")
    private Long clienteId;

    @Column(name = "SUCURSAL_ID")
    private Long sucursalId;

    @Column(name = "TOTAL")
    private BigDecimal total;

    @Column(name = "ESTADO", insertable = false, updatable = false)
    private String estado;

    @Column(name = "CODIGO_RETIRO", insertable = false, updatable = false)
    private String codigoRetiro;

    @Column(name = "FECHA_PEDIDO", insertable = false, updatable = false)
    private LocalDateTime fechaPedido;
}
