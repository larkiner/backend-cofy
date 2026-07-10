package com.cafeteria.api.interno.sucursal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SucursalInternaRepository extends JpaRepository<SucursalInterna, Long> {

    Optional<SucursalInterna> findFirstByEstadoOrderByIdAsc(String estado);
}
