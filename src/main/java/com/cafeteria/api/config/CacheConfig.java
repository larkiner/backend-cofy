package com.cafeteria.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Caché en memoria para el catálogo público (menú y sucursales), que se
 * lee mucho y cambia poco. En memoria a propósito, coherente con el resto
 * del proyecto (rate-limit y blacklist también lo son) y con una sola
 * instancia; para varias instancias, sustituir por un caché distribuido.
 *
 * Coherencia: las escrituras de inventario por la API evictan el caché
 * (ver {@link com.cafeteria.api.interno.producto.ProductoAdminService}),
 * y {@link CacheEvictionScheduler} lo vacía periódicamente para acotar el
 * desfase cuando el catálogo cambia por fuera de la API (p. ej. SQL directo).
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String CACHE_MENU = "menu";
    public static final String CACHE_SUCURSALES = "sucursales";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CACHE_MENU, CACHE_SUCURSALES);
    }
}
