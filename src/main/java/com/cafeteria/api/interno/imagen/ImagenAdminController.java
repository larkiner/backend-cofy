package com.cafeteria.api.interno.imagen;

import com.cafeteria.api.interno.imagen.dto.ImagenSubidaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Subida de imágenes de producto. SOLO rol ADMIN: la restricción está en
 * SecurityConfig. No toca Oracle (no necesita transaction manager): solo guarda
 * el archivo y devuelve su URL.
 */
@RestController
@RequestMapping("/api/interno/imagenes")
@RequiredArgsConstructor
public class ImagenAdminController {

    private final ImagenService imagenService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagenSubidaResponse subir(@RequestParam("archivo") MultipartFile archivo) {
        return new ImagenSubidaResponse(imagenService.subirImagenProducto(archivo));
    }
}
