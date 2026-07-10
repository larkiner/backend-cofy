package com.cafeteria.api.interno.trabajador;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrabajadorPerfilRepository extends JpaRepository<TrabajadorPerfil, Long> {

    Optional<TrabajadorPerfil> findByEmailIgnoreCase(String email);

    List<TrabajadorPerfil> findAllByOrderByNombreAsc();
}
