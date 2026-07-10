package com.cafeteria.api.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vista VW_PEDIDOS_CLIENTE: lo que el cliente ve de sus pedidos. */
@Entity
@Immutable
@Table(name = "VW_PEDIDOS_CLIENTE")
@Getter
@NoArgsConstructor
public class PedidoClienteVista {

    @Id
    @Column(name = "PEDIDO_ID")
    private Long pedidoId;

    @Column(name = "CLIENTE_ID")
    private Long clienteId;

    @Column(name = "CODIGO_RETIRO")
    private String codigoRetiro;

    @Column(name = "FECHA_PEDIDO")
    private LocalDateTime fechaPedido;

    @Column(name = "FECHA_ENTREGA")
    private LocalDateTime fechaEntrega;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "TOTAL")
    private BigDecimal total;

    @Column(name = "SUCURSAL_NOMBRE")
    private String sucursalNombre;

    @Column(name = "SUCURSAL_DIRECCION")
    private String sucursalDireccion;
}
