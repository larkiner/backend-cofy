package com.cafeteria.api.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Vista VW_CLIENTE_AUTH: lo mínimo para verificar credenciales.
 * Es el ÚNICO camino por el que el backend lee el password_hash.
 */
@Entity
@Immutable
@Table(name = "VW_CLIENTE_AUTH")
@Getter
@NoArgsConstructor
public class ClienteAuth {

    @Id
    private Long id;

    private String email;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    private String estado;
}
