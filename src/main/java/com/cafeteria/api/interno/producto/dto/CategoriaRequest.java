package com.cafeteria.api.interno.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 50) String nombre,
        @Size(max = 200) String descripcion
) {
}
