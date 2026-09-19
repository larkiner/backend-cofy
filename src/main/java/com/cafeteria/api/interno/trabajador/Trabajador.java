package com.cafeteria.api.interno.trabajador;

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

import java.time.LocalDateTime;

/**
 * Tabla TRABAJADORES (INSERT para el registro con clave de invitación,
 * SELECT/UPDATE para gestión y cambio de contraseña).
 */
@Entity
@Table(name = "TRABAJADORES")
@Getter
@Setter
@NoArgsConstructor
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_trabajadores")
    @SequenceGenerator(name = "seq_trabajadores", sequenceName = "CAFETERIA_APP.SEQ_TRABAJADORES", allocationSize = 20)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TELEFONO")
    private String telefono;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    @Column(name = "ROL")
    private String rol;

    @Column(name = "SUCURSAL_ID")
    private Long sucursalId;

    @Column(name = "FECHA_CONTRATACION", insertable = false, updatable = false)
    private LocalDateTime fechaContratacion;

    @Column(name = "ESTADO", insertable = false, updatable = false)
    private String estado;
}
