package com.cafeteria.api.interno.sucursal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Tabla SUCURSALES (solo lectura desde el panel interno). */
@Entity
@Immutable
@Table(name = "SUCURSALES")
@Getter
@NoArgsConstructor
public class SucursalInterna {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "TELEFONO")
    private String telefono;

    @Column(name = "ESTADO")
    private String estado;
}
