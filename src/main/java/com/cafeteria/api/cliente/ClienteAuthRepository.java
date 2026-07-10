package com.cafeteria.api.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteAuthRepository extends JpaRepository<ClienteAuth, Long> {

    Optional<ClienteAuth> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
