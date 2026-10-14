package com.cafeteria.api.interno.imagen.dto;

/** URL pública de la imagen recién subida; el frontend la guarda en el producto. */
public record ImagenSubidaResponse(String url) {
}
