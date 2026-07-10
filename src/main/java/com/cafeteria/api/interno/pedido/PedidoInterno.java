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
 * Tabla PEDIDOS desde el PANEL INTERNO (SELECT + UPDATE).
 * El personal solo modifica: estado, trabajador que entrega y
 * fecha de entrega. El resto es de solo lectura.
 */
@Entity
@Table(name = "PEDIDOS")
@Getter
@Setter
@NoArgsConstructor
public class PedidoInterno {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "TRABAJADOR_ID")
    private Long trabajadorId;

    @Column(name = "FECHA_ENTREGA")
    private LocalDateTime fechaEntrega;

    @Column(name = "CODIGO_RETIRO", updatable = false)
    private String codigoRetiro;

    @Column(name = "CLIENTE_ID", updatable = false)
    private Long clienteId;

    @Column(name = "SUCURSAL_ID", updatable = false)
    private Long sucursalId;

    @Column(name = "TOTAL", updatable = false)
    private BigDecimal total;

    @Column(name = "FECHA_PEDIDO", updatable = false)
    private LocalDateTime fechaPedido;
}
