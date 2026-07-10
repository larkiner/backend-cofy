package com.cafeteria.api.interno.trabajador;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/** Vista VW_TRABAJADOR_PERFIL: datos del trabajador SIN password_hash. */
@Entity
@Immutable
@Table(name = "VW_TRABAJADOR_PERFIL")
@Getter
@NoArgsConstructor
public class TrabajadorPerfil {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TELEFONO")
    private String telefono;

    @Column(name = "ROL")
    private String rol;

    @Column(name = "SUCURSAL_ID")
    private Long sucursalId;

    @Column(name = "SUCURSAL_NOMBRE")
    private String sucursalNombre;

    @Column(name = "FECHA_CONTRATACION")
    private LocalDateTime fechaContratacion;

    @Column(name = "ESTADO")
    private String estado;
}
