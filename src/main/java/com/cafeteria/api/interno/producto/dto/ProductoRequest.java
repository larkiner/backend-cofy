package com.cafeteria.api.interno.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 300) String descripcion,
        @NotNull @Positive BigDecimal precio,
        @NotNull Long categoriaId,
        @Pattern(regexp = "S|N", message = "disponible debe ser 'S' o 'N'")
        String disponible,
        @Size(max = 300) String imagenUrl
) {
}
