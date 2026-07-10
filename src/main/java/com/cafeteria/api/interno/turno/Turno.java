package com.cafeteria.api.interno.turno;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tabla TURNOS. Los crean supervisores/admin; cada empleado
 * consulta los suyos.
 */
@Entity
@Table(name = "TURNOS")
@Getter
@Setter
@NoArgsConstructor
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_turnos")
    @SequenceGenerator(name = "seq_turnos", sequenceName = "CAFETERIA_APP.SEQ_TURNOS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TRABAJADOR_ID")
    private Long trabajadorId;

    @Column(name = "FECHA")
    private LocalDate fecha;

    @Column(name = "HORA_INICIO")
    private LocalDateTime horaInicio;

    @Column(name = "HORA_FIN")
    private LocalDateTime horaFin;
}
