package com.cafeteria.api.interno.metrica;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vista VW_METRICAS_SUCURSAL: ventas por sucursal y día, en vivo.
 * Solo SUPERVISOR/ADMIN (ver SecurityConfig).
 */
@Entity
@Immutable
@IdClass(MetricaSucursalId.class)
@Table(name = "VW_METRICAS_SUCURSAL")
@Getter
@NoArgsConstructor
public class MetricaSucursal {

    @Id
    @Column(name = "SUCURSAL_ID")
    private Long sucursalId;

    @Id
    @Column(name = "FECHA")
    private LocalDate fecha;

    @Column(name = "SUCURSAL_NOMBRE")
    private String sucursalNombre;

    @Column(name = "CANTIDAD_PEDIDOS")
    private Long cantidadPedidos;

    @Column(name = "TOTAL_VENTAS")
    private BigDecimal totalVentas;
}
