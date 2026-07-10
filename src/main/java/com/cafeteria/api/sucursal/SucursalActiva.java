package com.cafeteria.api.sucursal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Vista VW_SUCURSALES_ACTIVAS: donde el cliente puede retirar su pedido. */
@Entity
@Immutable
@Table(name = "VW_SUCURSALES_ACTIVAS")
@Getter
@NoArgsConstructor
public class SucursalActiva {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "TELEFONO")
    private String telefono;
}
