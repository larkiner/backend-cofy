package com.cafeteria.api.menu;

import com.cafeteria.api.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    /** El menú completo se cachea (se lee mucho, cambia poco); las escrituras
     *  de inventario evictan el caché "menu" por completo. */
    @Cacheable(CacheConfig.CACHE_MENU)
    @Transactional(readOnly = true)
    public List<MenuItem> obtenerMenu() {
        return menuRepository.findAllByOrderByCategoriaAscNombreAsc();
    }

    /** Se cachea bajo el mismo caché "menu" con clave por categoría (prefijada
     *  para no colisionar con la clave vacía del menú completo). */
    @Cacheable(value = CacheConfig.CACHE_MENU, key = "'categoria:' + #categoriaId")
    @Transactional(readOnly = true)
    public List<MenuItem> obtenerMenuPorCategoria(Long categoriaId) {
        return menuRepository.findByCategoriaIdOrderByNombreAsc(categoriaId);
    }
}
