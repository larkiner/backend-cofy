package com.cafeteria.api.interno.trabajador;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Vista VW_TRABAJADOR_AUTH: lo mínimo para el login de empleados. */
@Entity
@Immutable
@Table(name = "VW_TRABAJADOR_AUTH")
@Getter
@NoArgsConstructor
public class TrabajadorAuth {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    @Column(name = "ROL")
    private String rol;

    @Column(name = "ESTADO")
    private String estado;
}
