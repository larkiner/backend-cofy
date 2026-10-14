package com.cafeteria.api.interno.imagen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Guarda las imágenes en el bucket público de Supabase Storage. La clave de
 * servicio vive SOLO en el servidor (variable {@code SUPABASE_SERVICE_KEY}): el
 * navegador nunca la ve ni escribe directo en el bucket.
 *
 * Si falta la URL o la clave, la API arranca igual (la subida es opcional) y el
 * endpoint responde 503 hasta que se configuren.
 */
@Component
public class SupabaseAlmacenImagenes implements AlmacenImagenes {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAlmacenImagenes.class);

    private final RestClient http;
    private final String baseUrl;
    private final String claveServicio;
    private final String bucket;

    /**
     * Constructor de Spring. No se inyecta un {@code RestClient.Builder}: en
     * Spring Boot 4 ese bean no se crea solo con el starter web actual, y su
     * ausencia impedía arrancar la API.
     */
    @Autowired
    public SupabaseAlmacenImagenes(@Value("${app.supabase.url:}") String url,
                                   @Value("${app.supabase.service-key:}") String claveServicio,
                                   @Value("${app.supabase.bucket:imagenes}") String bucket) {
        this(RestClient.builder(), url, claveServicio, bucket);
    }

    /** Para pruebas: permite enlazar un servidor simulado al builder. */
    SupabaseAlmacenImagenes(RestClient.Builder builder, String url, String claveServicio, String bucket) {
        this.http = builder.build();
        this.baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.claveServicio = claveServicio;
        this.bucket = bucket;
    }

    @Override
    public String guardar(String ruta, String tipoContenido, byte[] contenido) {
        if (baseUrl.isBlank() || claveServicio.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La subida de imágenes no está configurada en el servidor.");
        }
        try {
            http.post()
                    .uri(baseUrl + "/storage/v1/object/" + bucket + "/" + ruta)
                    .headers(h -> {
                        h.set("apikey", claveServicio);
                        // Las claves nuevas (sb_secret_...) no son JWT: solo van en apikey.
                        if (claveServicio.startsWith("eyJ")) {
                            h.setBearerAuth(claveServicio);
                        }
                        h.set("x-upsert", "false");
                    })
                    .contentType(MediaType.parseMediaType(tipoContenido))
                    .body(contenido)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // Sin el cuerpo de la respuesta ni la clave: solo el tipo de fallo.
            log.warn("Supabase Storage rechazó la subida de {}: {}", ruta, e.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo guardar la imagen. Intenta de nuevo.");
        }
        return baseUrl + "/storage/v1/object/public/" + bucket + "/" + ruta;
    }
}
