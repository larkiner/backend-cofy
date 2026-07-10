package com.cafeteria.api.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientePerfilRepository extends JpaRepository<ClientePerfil, Long> {

    Optional<ClientePerfil> findByEmailIgnoreCase(String email);
}
