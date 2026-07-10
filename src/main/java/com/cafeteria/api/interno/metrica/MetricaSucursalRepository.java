package com.cafeteria.api.interno.metrica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetricaSucursalRepository extends JpaRepository<MetricaSucursal, MetricaSucursalId> {

    List<MetricaSucursal> findAllByOrderByFechaDescSucursalIdAsc();

    List<MetricaSucursal> findBySucursalIdOrderByFechaDesc(Long sucursalId);
}
