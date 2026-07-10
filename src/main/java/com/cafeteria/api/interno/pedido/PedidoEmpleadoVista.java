package com.cafeteria.api.interno.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vista VW_PEDIDOS_EMPLEADO: tablero operativo con datos del cliente. */
@Entity
@Immutable
@Table(name = "VW_PEDIDOS_EMPLEADO")
@Getter
@NoArgsConstructor
public class PedidoEmpleadoVista {

    @Id
    @Column(name = "PEDIDO_ID")
    private Long pedidoId;

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

    @Column(name = "SUCURSAL_ID")
    private Long sucursalId;

    @Column(name = "TRABAJADOR_ID")
    private Long trabajadorId;

    @Column(name = "CLIENTE_ID")
    private Long clienteId;

    @Column(name = "CLIENTE_NOMBRE")
    private String clienteNombre;

    @Column(name = "CLIENTE_TELEFONO")
    private String clienteTelefono;
}
