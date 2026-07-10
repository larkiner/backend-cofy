package com.cafeteria.api.interno.trabajador;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrabajadorAuthRepository extends JpaRepository<TrabajadorAuth, Long> {

    Optional<TrabajadorAuth> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
