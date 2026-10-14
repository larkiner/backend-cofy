package com.cafeteria.api.interno.imagen;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

/**
 * Valida y guarda imágenes de producto. No confía en lo que declara el cliente
 * (nombre ni Content-Type): el tipo real sale de los primeros bytes del archivo,
 * y el nombre guardado es siempre uno nuevo (nunca el original), así no hay
 * rutas manipuladas ni archivos que se pisen entre sí.
 */
@Service
@RequiredArgsConstructor
public class ImagenService {

    /** Mismo tope que el bucket de Supabase (5 MB). */
    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private final AlmacenImagenes almacen;

    /** Tipos admitidos: los mismos del bucket. */
    private enum Tipo {
        JPEG("image/jpeg", "jpg"), PNG("image/png", "png"), WEBP("image/webp", "webp");

        final String mime;
        final String extension;

        Tipo(String mime, String extension) {
            this.mime = mime;
            this.extension = extension;
        }
    }

    public String subirImagenProducto(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen.");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "La imagen pesa más de 5 MB.");
        }
        byte[] bytes;
        try {
            bytes = archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen.");
        }
        Tipo tipo = detectar(bytes);
        if (tipo == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Solo se admiten imágenes JPG, PNG o WEBP.");
        }
        String ruta = "productos/" + UUID.randomUUID() + "." + tipo.extension;
        return almacen.guardar(ruta, tipo.mime, bytes);
    }

    /** Firma (magic bytes) de cada formato. */
    private static Tipo detectar(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return Tipo.JPEG;
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return Tipo.PNG;
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return Tipo.WEBP;
        }
        return null;
    }
}
