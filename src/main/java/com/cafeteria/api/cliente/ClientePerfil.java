package com.cafeteria.api.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Vista VW_CLIENTE_PERFIL: datos del cliente SIN password_hash.
 * Es lo que se devuelve al frontend (nunca la tabla base).
 */
@Entity
@Immutable
@Table(name = "VW_CLIENTE_PERFIL")
@Getter
@NoArgsConstructor
public class ClientePerfil {

    @Id
    private Long id;

    private String nombre;

    private String email;

    private String telefono;

    @Column(name = "FECHA_REGISTRO")
    private LocalDateTime fechaRegistro;

    private String estado;
}
