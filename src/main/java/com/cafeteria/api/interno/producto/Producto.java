package com.cafeteria.api.interno.producto;

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
 * Tabla PRODUCTOS (gestión de inventario, SOLO rol ADMIN).
 * Requiere haber ejecutado el Script 4 (grants a ROL_SUPERVISOR).
 */
@Entity
@Table(name = "PRODUCTOS")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_productos")
    @SequenceGenerator(name = "seq_productos", sequenceName = "CAFETERIA_APP.SEQ_PRODUCTOS", allocationSize = 20)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "PRECIO")
    private BigDecimal precio;

    @Column(name = "CATEGORIA_ID")
    private Long categoriaId;

    @Column(name = "DISPONIBLE")
    private String disponible;

    @Column(name = "IMAGEN_URL")
    private String imagenUrl;
}
