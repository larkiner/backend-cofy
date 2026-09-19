package com.cafeteria.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Vacía periódicamente los cachés de catálogo. Acota el desfase cuando el
 * menú o las sucursales cambian por fuera de la API (por ejemplo, un UPDATE
 * por SQL directo sobre PRODUCTOS): esos cambios no pasan por el
 * {@code @CacheEvict} de la aplicación, así que sin este barrido quedarían
 * ocultos hasta reiniciar. Con las escrituras de la API el caché se evicta
 * al instante; este barrido es solo la red de seguridad para el resto.
 *
 * El intervalo se configura con app.cache.evict-ms (por defecto 120000 = 2 min).
 */
@Component
@RequiredArgsConstructor
public class CacheEvictionScheduler {

    private final CacheManager cacheManager;

    @Scheduled(fixedRateString = "${app.cache.evict-ms:120000}")
    public void limpiarCatalogo() {
        for (String nombre : List.of(CacheConfig.CACHE_MENU, CacheConfig.CACHE_SUCURSALES)) {
            Cache cache = cacheManager.getCache(nombre);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
