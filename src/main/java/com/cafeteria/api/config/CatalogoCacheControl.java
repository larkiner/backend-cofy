package com.cafeteria.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cabecera Cache-Control de las respuestas públicas del catálogo (menú y
 * sucursales): se leen en cada navegación y cambian poco, así que el
 * navegador puede reutilizarlas unos segundos sin volver a la API.
 *
 * Hay que fijarla explícitamente: sin ella Spring Security responde
 * "no-cache, no-store" y cada visita llega hasta Docker y Oracle.
 * El desfase máximo visible tras un cambio de inventario es este max-age
 * (app.cache.http-max-age-seg, 60 s por defecto) más el barrido del caché
 * del servidor (CacheEvictionScheduler). Los precios de un pedido siempre se
 * releen de la BD, así que un menú ligeramente viejo nunca cambia un cobro.
 */
@Component
public class CatalogoCacheControl {

    private final CacheControl cacheControl;

    public CatalogoCacheControl(@Value("${app.cache.http-max-age-seg:60}") long maxAgeSeg) {
        this.cacheControl = CacheControl.maxAge(Duration.ofSeconds(maxAgeSeg)).cachePublic();
    }

    public <T> ResponseEntity<T> ok(T cuerpo) {
        return ResponseEntity.ok().cacheControl(cacheControl).body(cuerpo);
    }
}
