package com.cafeteria.api.cliente;

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
 * Mapea la tabla CLIENTES pero SOLO para el registro (INSERT):
 * ROL_CLIENTE no tiene SELECT sobre la tabla base. Las lecturas
 * se hacen por las vistas (ClienteAuth / ClientePerfil).
 *
 * El id lo asigna el trigger TRG_CLIENTES_BI en la BD; la estrategia
 * IDENTITY hace que Hibernate lo recupere con RETURNING id INTO.
 */
@Entity
@Table(name = "CLIENTES")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_clientes")
    @SequenceGenerator(name = "seq_clientes", sequenceName = "CAFETERIA_APP.SEQ_CLIENTES", allocationSize = 20)
    private Long id;

    private String nombre;

    private String email;

    private String telefono;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    // Los asigna la BD por DEFAULT (SYSDATE / 'ACTIVO'): no se insertan
    @Column(name = "FECHA_REGISTRO", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(insertable = false, updatable = false)
    private String estado;
}
