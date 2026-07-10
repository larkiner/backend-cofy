package com.cafeteria.api.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findAllByOrderByCategoriaAscNombreAsc();

    List<MenuItem> findByCategoriaIdOrderByNombreAsc(Long categoriaId);
}
