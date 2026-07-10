package com.cafeteria.api.interno.trabajador.dto;

/** Datos del trabajador recién creado (sin la contraseña, obviamente). */
public record TrabajadorCreadoResponse(
        Long id,
        String nombre,
        String email,
        String rol,
        Long sucursalId
) {
}
