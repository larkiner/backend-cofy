package com.cafeteria.api.sucursal;

import com.cafeteria.api.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sucursales activas (público). Se cachea porque cambian muy poco y se
 * consultan en cada flujo de pedido para elegir dónde retirar.
 */
@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalActivaRepository sucursalActivaRepository;

    @Cacheable(CacheConfig.CACHE_SUCURSALES)
    @Transactional(readOnly = true)
    public List<SucursalActiva> listarActivas() {
        return sucursalActivaRepository.findAll();
    }
}
