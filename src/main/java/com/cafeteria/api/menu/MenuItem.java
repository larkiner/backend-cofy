package com.cafeteria.api.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Item del menú, mapeado a la vista VW_MENU (no a la tabla PRODUCTOS).
 * El usuario de conexión CAFETERIA_EMPLEADO_APP solo tiene SELECT sobre
 * la vista, por eso la entidad es de solo lectura (@Immutable).
 */
@Entity
@Immutable
@Table(name = "VW_MENU")
@Getter
@NoArgsConstructor
public class MenuItem {

    @Id
    @Column(name = "PRODUCTO_ID")
    private Long productoId;

    @Column(name = "PRODUCTO_NOMBRE")
    private String nombre;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "PRECIO")
    private BigDecimal precio;

    @Column(name = "IMAGEN_URL")
    private String imagenUrl;

    @Column(name = "CATEGORIA_ID")
    private Long categoriaId;

    @Column(name = "CATEGORIA_NOMBRE")
    private String categoria;
}
