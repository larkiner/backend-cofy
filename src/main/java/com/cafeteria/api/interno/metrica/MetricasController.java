package com.cafeteria.api.interno.metrica;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Métricas de ventas por sucursal (solo SUPERVISOR/ADMIN). */
@RestController
@RequestMapping("/api/interno/metricas")
@RequiredArgsConstructor
public class MetricasController {

    private final MetricaSucursalRepository metricaSucursalRepository;

    @GetMapping
    public List<MetricaSucursal> metricas(
            @RequestParam(required = false) Long sucursalId) {
        if (sucursalId != null) {
            return metricaSucursalRepository.findBySucursalIdOrderByFechaDesc(sucursalId);
        }
        return metricaSucursalRepository.findAllByOrderByFechaDescSucursalIdAsc();
    }
}
