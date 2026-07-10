package com.cafeteria.api.interno.turno.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TurnoRequest(
        @NotNull Long trabajadorId,
        @NotNull LocalDate fecha,
        @NotNull LocalDateTime horaInicio,
        @NotNull LocalDateTime horaFin
) {
}
